package com.afoxxvi.alopex.component.notify

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.drawable.Drawable
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import java.time.LocalDateTime

class NotificationGroup(context: Context, @get:Bindable val packageName: String, val packageId: Int) : BaseObservable() {
    private var appName: String? = null

    @get:Bindable
    var appIcon: Drawable? = null
    private val wrapperNotificationList: MutableList<WrapperNotification>

    init {
        wrapperNotificationList = ArrayList()
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
        get() = if (wrapperNotificationList.isNotEmpty()) {
            wrapperNotificationList[0].title
        } else "<Empty>"

    @get:Bindable
    val latestText: String?
        get() = if (wrapperNotificationList.isNotEmpty()) {
            wrapperNotificationList[0].text
        } else "<No notification recently>"

    @get:Bindable
    val latestTimeText: String
        get() = if (wrapperNotificationList.isNotEmpty()) {
            wrapperNotificationList[0].dateText
        } else ""

    @get:Bindable
    val notifyCount: Int
        get() = wrapperNotificationList.size
    val latestTime: LocalDateTime
        get() = if (wrapperNotificationList.isNotEmpty()) {
            wrapperNotificationList[0].time
        } else LocalDateTime.MIN
    val earliestNotifyTime: LocalDateTime
        get() = if (wrapperNotificationList.isNotEmpty()) {
            wrapperNotificationList[wrapperNotificationList.size - 1].time
        } else LocalDateTime.now()

    private fun checkNotify(wrapperNotification: WrapperNotification) {}
    fun addNotify(wrapperNotification: WrapperNotification) {
        wrapperNotificationList.add(wrapperNotification)
    }

    fun addNotify(index: Int, wrapperNotification: WrapperNotification) {
        wrapperNotificationList.add(index, wrapperNotification)
    }

    fun getNotification(index: Int): WrapperNotification {
        return wrapperNotificationList[index]
    }

    fun limitCount(max: Int) {
        wrapperNotificationList.subList(max, notifyCount).clear()
    }
}