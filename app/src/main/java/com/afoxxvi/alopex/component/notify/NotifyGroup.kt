package com.afoxxvi.alopex.component.notify

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.drawable.Drawable
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import java.time.LocalDateTime

class NotifyGroup(context: Context, @get:Bindable val packageName: String, val packageId: Int) : BaseObservable() {
    private var appName: String? = null

    @get:Bindable
    var appIcon: Drawable? = null
    private val notifyList: MutableList<Notify>

    init {
        notifyList = ArrayList()
        val packageManager = context.packageManager
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES or PackageManager.GET_META_DATA)
            appName = info.applicationInfo.loadLabel(packageManager).toString()
            appIcon = info.applicationInfo.loadIcon(packageManager)
        } catch (e: NameNotFoundException) {
            appName = null
            appIcon = null
        }
    }

    @Bindable
    fun getAppName(): String {
        return appName ?: packageName
    }

    @get:Bindable
    val latestTitle: String?
        get() = if (notifyList.isNotEmpty()) {
            notifyList[0].title
        } else "<Empty>"

    @get:Bindable
    val latestText: String?
        get() = if (notifyList.isNotEmpty()) {
            notifyList[0].text
        } else "<No notification recently>"

    @get:Bindable
    val latestTimeText: String
        get() = if (notifyList.isNotEmpty()) {
            notifyList[0].dateText
        } else ""

    @get:Bindable
    val notifyCount: Int
        get() = notifyList.size
    val latestTime: LocalDateTime
        get() = if (notifyList.isNotEmpty()) {
            notifyList[0].time
        } else LocalDateTime.MIN
    val earliestNotifyTime: LocalDateTime
        get() = if (notifyList.isNotEmpty()) {
            notifyList[notifyList.size - 1].time
        } else LocalDateTime.now()

    private fun checkNotify(notify: Notify) {}
    fun addNotify(notify: Notify) {
        notifyList.add(notify)
    }

    fun addNotify(index: Int, notify: Notify) {
        notifyList.add(index, notify)
    }

    fun getNotify(index: Int): Notify {
        return notifyList[index]
    }

    fun limitCount(max: Int) {
        notifyList.subList(max, notifyCount).clear()
    }
}