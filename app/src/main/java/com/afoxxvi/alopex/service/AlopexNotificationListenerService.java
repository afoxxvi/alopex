package com.afoxxvi.alopex.service;

import static com.afoxxvi.alopex.AlopexView.TAG;
import static com.afoxxvi.alopex.MainActivity.CHANNEL_ONE_ID;

import android.app.Notification;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Message;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.afoxxvi.alopex.AlopexView;
import com.afoxxvi.alopex.R;
import com.afoxxvi.alopex.component.filter.AlopexFilterManager;
import com.afoxxvi.alopex.ui.fragment.NotificationFragment;
import com.afoxxvi.alopex.util.Pair;
import com.afoxxvi.alopex.util.Triplet;

import java.util.HashMap;
import java.util.Map;

public class AlopexNotificationListenerService extends NotificationListenerService {
    public static boolean active = false;
    private static final Map<String, Pair<String, String>> LATEST_NOTIFICATION_MAP = new HashMap<>();
    public static final String PROPERTY_KEY_LAST_NOTIFICATION_MILLS = "lastNotificationMills";
    private static long lastNotificationMills = 0;

    public AlopexNotificationListenerService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void handleNotification(StatusBarNotification sbn) {
        Bundle bundle = sbn.getNotification().extras;
        String pkg = sbn.getPackageName();
        if (pkg.equals(getApplication().getPackageName())) {
            return;
        }
        String title = bundle.getString(Notification.EXTRA_TITLE, "");
        String text = bundle.getString(Notification.EXTRA_TEXT, "");
        if (LATEST_NOTIFICATION_MAP.containsKey(pkg)) {
            Pair<String, String> old = LATEST_NOTIFICATION_MAP.get(pkg);
            if (old != null && old.a.equals(title) && old.b.equals(text)) {
                return;
            }
        }
        LATEST_NOTIFICATION_MAP.put(pkg, new Pair<>(title, text));
        if (AlopexView.handlerNotification != null) {
            Message msg = Message.obtain(AlopexView.handlerNotification, NotificationFragment.WHAT_NOTIFICATION_LISTENER_SERVICE);
            Bundle bd = new Bundle();
            bd.putString("package", pkg);
            bd.putString("title", title);
            bd.putString("content", text);
            bd.putLong("time", sbn.getPostTime());
            msg.setData(bd);
            msg.sendToTarget();
        }
        Triplet<Boolean, Boolean, Boolean> pair = AlopexFilterManager.getInstance().isFiltered(pkg, title, text, true);
        if (pair.a) {
            Notification ntf = sbn.getNotification();
            Icon icon = ntf.getSmallIcon();
            Icon large = ntf.getLargeIcon();
            Notification notification = new Notification.Builder(this, CHANNEL_ONE_ID)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setContentIntent(ntf.contentIntent)
                    .setSmallIcon(icon)
                    .setLargeIcon(large)
                    //.setTimeoutAfter(1000)
                    .build();
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
            managerCompat.notify(1, notification);
        }
        if (pair.b) {
            cancelNotification(sbn);
        }
    }

    private void saveProperties() {
        AlopexView.getProperties().edit().putLong(PROPERTY_KEY_LAST_NOTIFICATION_MILLS, lastNotificationMills).apply();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        lastNotificationMills = sbn.getPostTime();
        saveProperties();
        handleNotification(sbn);
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        lastNotificationMills = AlopexView.getProperties().getLong(PROPERTY_KEY_LAST_NOTIFICATION_MILLS, 0);
        Log.i(TAG, "onListenerConnected: ");
        active = true;
        Notification notification = new Notification.Builder(this, CHANNEL_ONE_ID)
                .setAutoCancel(true)
                .setContentTitle("Listener Connected")
                .setContentText("at " + AlopexView.getDateTimeString())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(1000, notification);
        //startForeground(1, notification);
        AlopexView.notifyConnect();
        long maxMills = lastNotificationMills;
        for (StatusBarNotification activeNotification : getActiveNotifications()) {
            if (activeNotification.getPostTime() > lastNotificationMills) {
                handleNotification(activeNotification);
                maxMills = Math.max(maxMills, activeNotification.getPostTime());
            }
        }
        lastNotificationMills = maxMills;
        saveProperties();
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.i(TAG, "onListenerDisconnected: ");
        active = false;
        Notification notification = new Notification.Builder(this, CHANNEL_ONE_ID)
                .setAutoCancel(true)
                .setContentTitle("Listener Disconnected")
                .setContentText("at " + AlopexView.getDateTimeString())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(1000, notification);
        AlopexView.notifyDisconnect();
    }

    public void cancelNotification(StatusBarNotification sbn) {
        cancelNotification(sbn.getKey());
    }
}