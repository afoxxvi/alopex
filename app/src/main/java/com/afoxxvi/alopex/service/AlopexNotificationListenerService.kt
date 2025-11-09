package com.afoxxvi.alopex.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Message
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.afoxxvi.alopex.Alopex
import com.afoxxvi.alopex.MainActivity
import com.afoxxvi.alopex.R
import com.afoxxvi.alopex.component.filter.Filters
import com.afoxxvi.alopex.component.notify.Notifications
import com.afoxxvi.alopex.component.notify.WrapperNotification
import com.afoxxvi.alopex.ui.fragment.NotificationFragment
import com.afoxxvi.alopex.util.FoxTools

class AlopexNotificationListenerService : NotificationListenerService() {
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun sendNotification(notification: Notification, id: Int) {
        val managerCompat = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        managerCompat.notify(id, notification)
    }

    private fun handleNotification(sbn: StatusBarNotification) {
        val bundle = sbn.notification.extras
        val pkg = sbn.packageName
        if (pkg == application.packageName) return
        val title = bundle.getString(Notification.EXTRA_TITLE, "")
        val text = bundle.getString(Notification.EXTRA_TEXT, "")
        if (LATEST_NOTIFICATION_MAP.containsKey(pkg)) {
            val old = LATEST_NOTIFICATION_MAP[pkg]
            if (old != null && old.first == title && old.second == text) {
                return
            }
        }
        LATEST_NOTIFICATION_MAP[pkg] = Pair(title, text)
        Alopex.handlerNotification?.let {
            val msg = Message.obtain(it, NotificationFragment.WHAT_NOTIFICATION_LISTENER_SERVICE)
            val wrapperNotification = WrapperNotification(title, text, FoxTools.getLocalDateTimeFromMills(System.currentTimeMillis()))
            val fromIndex = Notifications.newNotify(applicationContext, pkg, wrapperNotification)
            msg.arg1 = fromIndex
            msg.sendToTarget()
        }
        val result = Filters.passNotification(pkg, title, text)
        if (result.doNotify) {
            val ntf = sbn.notification
            val icon = ntf.smallIcon
            val large = ntf.getLargeIcon()
            val notification = Notification.Builder(this, Alopex.CHANNEL_ID)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(ntf.contentIntent)
                .setSmallIcon(icon)
                .setLargeIcon(large) //.setTimeoutAfter(1000)
                .build()
            sendNotification(notification, 1000)
        }
        if (result.doCancel) {
            cancelNotification(sbn.key)
        }
    }

    private fun saveProperties() {
        Alopex.properties!!.edit().putLong(PROPERTY_KEY_LAST_NOTIFICATION_MILLS, lastNotificationMills).apply()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        lastNotificationMills = sbn.postTime
        saveProperties()
        handleNotification(sbn)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onListenerConnected() {
        super.onListenerConnected()
        lastNotificationMills = Alopex.properties!!.getLong(PROPERTY_KEY_LAST_NOTIFICATION_MILLS, 0)
        Log.i(Alopex.TAG, "onListenerConnected: ")
        active = true
        val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val notification =
            Notification.Builder(this, Alopex.CHANNEL_ID)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentTitle("Alopex is running")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
        sendNotification(notification, 1)
        startForeground(1, notification)
        Alopex.notifyConnect()
        var maxMills = lastNotificationMills
        for (activeNotification in activeNotifications) {
            if (activeNotification.postTime > lastNotificationMills) {
                handleNotification(activeNotification)
                maxMills = maxMills.coerceAtLeast(activeNotification.postTime)
            }
        }
        lastNotificationMills = maxMills
        saveProperties()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Alopex.notifyDisconnect()
    }

    companion object {
        var active = false
        private val LATEST_NOTIFICATION_MAP: MutableMap<String, Pair<String, String>> = HashMap()
        const val PROPERTY_KEY_LAST_NOTIFICATION_MILLS = "lastNotificationMills"
        private var lastNotificationMills: Long = 0
    }
}