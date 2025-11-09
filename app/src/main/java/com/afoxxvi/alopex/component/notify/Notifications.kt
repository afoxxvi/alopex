package com.afoxxvi.alopex.component.notify

import android.annotation.SuppressLint
import android.content.Context
import com.afoxxvi.alopex.Alopex
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Notifications {
    private val notificationGroupList: MutableList<NotificationGroup>
    private val packageIdMap: MutableMap<String, Int>
    private var notificationStorage: NotificationStorage? = null

    @SuppressLint("DefaultLocale")
    fun newNotify(context: Context, pkg: String, wrapperNotification: WrapperNotification): Int {
        var id = packageIdMap[pkg]
        val db = notificationStorage!!.readableDatabase
        val fromIndex: Int
        if (id == null) {
            val stmt = db.compileStatement("insert into Package(package) values(?)")
            stmt.bindString(1, pkg)
            id = Math.toIntExact(stmt.executeInsert())
            if (id != -1) {
                packageIdMap[pkg] = id
                val notificationGroup = NotificationGroup(context, pkg, id)
                notificationGroup.addNotify(0, wrapperNotification)
                notificationGroupList.add(0, notificationGroup)
            }
            fromIndex = if (id == -1) -1 else notificationGroupList.size
        } else {
            var i = 0
            while (i < notificationGroupList.size) {
                val notifyGroup = notificationGroupList[i]
                if (notifyGroup.packageId == id) {
                    if (pkg != notifyGroup.packageName) {
                        Alopex.log(
                            String.format(
                                "error on new notify: different package name. new notify pkg is %s, found id is %d, found pkg is %s",
                                pkg, id, notifyGroup.packageName
                            )
                        )
                    }
                    notifyGroup.addNotify(0, wrapperNotification)
                    if (notifyGroup.notifyCount > MAX_NOTIFY_COUNT) {
                        notifyGroup.limitCount(MAX_NOTIFY_COUNT)
                    }
                    break
                }
                i++
            }
            val notificationGroup = if (i < notificationGroupList.size) {
                notificationGroupList.removeAt(i)
            } else NotificationGroup(context, pkg, id)
            notificationGroupList.add(0, notificationGroup)
            notificationGroup.notifyChange()
            fromIndex = i
        }
        if (id != -1) {
            val stmt =
                db.compileStatement("insert into Notify(package, title, content, time) values(?,?,?,?)")
            stmt.bindLong(1, id.toLong())
            stmt.bindString(2, wrapperNotification.title)
            stmt.bindString(3, wrapperNotification.text)
            stmt.bindString(4, wrapperNotification.time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            stmt.executeInsert()
        }
        return fromIndex
    }

    fun getNotifyGroupList(): List<NotificationGroup> {
        return notificationGroupList
    }

    fun requestMore(group: NotificationGroup, increment: Int) {
        val db = notificationStorage!!.readableDatabase
        val cursor = db.rawQuery(
            "select title, content, time from Notify where package = ? and time < ? order by time desc limit ?",
            arrayOf(
                group.packageId.toString(),
                group.earliestNotifyTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                increment.toString()
            )
        )
        if (cursor.moveToFirst()) {
            var title: String?
            var text: String?
            var time: LocalDateTime?
            do {
                title = cursor.getString(0)
                text = cursor.getString(1)
                time = LocalDateTime.parse(
                    cursor.getString(2),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                )
                val wrapperNotification = WrapperNotification(title, text, time)
                group.addNotify(wrapperNotification)
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    fun init(context: Context) {
        notificationStorage = NotificationStorage(context, "notify.db")
        notificationGroupList.clear()
        packageIdMap.clear()
        kotlin.runCatching { loadFromDatabase(context) }
    }

    private fun loadFromDatabase(context: Context) {
        val db = notificationStorage!!.readableDatabase
        var cursor = db.rawQuery("select id, package from Package order by id", arrayOfNulls(0))
        val indexMap: MutableMap<Int, NotificationGroup> = HashMap(cursor.count)
        if (cursor.moveToFirst()) {
            var id: Int
            var pkg: String
            do {
                id = cursor.getInt(0)
                pkg = cursor.getString(1)
                val group = NotificationGroup(context!!, pkg, id)
                notificationGroupList.add(group)
                packageIdMap[pkg] = id
                indexMap[id] = group
            } while (cursor.moveToNext())
        }
        cursor.close()
        val stmt = db.compileStatement("delete from Notify where time < ?")
        stmt.bindString(
            1,
            LocalDateTime.now().minusDays(14).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        )
        stmt.executeUpdateDelete()
        cursor = db.rawQuery(
            "select package, title, content, time from Notify order by time desc limit ?",
            arrayOf(INITIAL_MAX_TOTAL_NOTIFY_COUNT.toString())
        )
        if (cursor.moveToFirst()) {
            var pkg: Int
            var title: String?
            var text: String?
            var time: LocalDateTime?
            do {
                pkg = cursor.getInt(0)
                title = cursor.getString(1)
                text = cursor.getString(2)
                time = LocalDateTime.parse(
                    cursor.getString(3),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                )
                val wrapperNotification = WrapperNotification(title, text, time)
                val i = indexMap[pkg]
                if (i != null && i.notifyCount < INITIAL_MAX_NOTIFY_COUNT) {
                    i.addNotify(wrapperNotification)
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        notificationGroupList.sortWith { g1: NotificationGroup, g2: NotificationGroup -> -g1.latestTime.compareTo(g2.latestTime) }
        db.close()
    }

    private const val INITIAL_MAX_NOTIFY_COUNT = 30
    private const val MAX_NOTIFY_COUNT = 30
    private const val INITIAL_MAX_TOTAL_NOTIFY_COUNT = 200

    init {
        notificationGroupList = ArrayList()
        packageIdMap = HashMap(32)
    }
}