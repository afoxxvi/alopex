package com.afoxxvi.alopex.service;

import static com.afoxxvi.alopex.AlopexView.TAG;
import static com.afoxxvi.alopex.MainActivity.CHANNEL_ONE_ID;

import android.app.Notification;
import android.content.ComponentName;
import android.content.pm.PackageManager;
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
    private static boolean rebinding = false;
    private static final Map<String, Pair<String, String>> LATEST_NOTIFICATION_MAP = new HashMap<>();

    public AlopexNotificationListenerService() {
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
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
            msg.setData(bd);
            msg.sendToTarget();
        }
        Triplet<Boolean, Boolean, Boolean> pair = AlopexFilterManager.getInstance().isFiltered(pkg, title, text, true);
        if (pair.a) {
            Notification notification = new Notification.Builder(this, CHANNEL_ONE_ID)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setTimeoutAfter(1000)
                    .build();
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
            managerCompat.notify(1, notification);
        }
        if (pair.b) {
            cancelNotification(sbn);
        }
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.i(TAG, "onListenerConnected: ");
        active = true;
        Notification notification = new Notification.Builder(this, CHANNEL_ONE_ID)
                .setAutoCancel(true)
                .setContentTitle("Listener Connected")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setTimeoutAfter(10000)
                .build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(1000, notification);
        for (StatusBarNotification sbn : getActiveNotifications()) {

        }
    }

    public void rebind() {
        rebinding = true;
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(getApplicationContext(), AlopexNotificationListenerService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        Log.i(TAG, "rebind: Disabled");
        pm.setComponentEnabledSetting(new ComponentName(getApplicationContext(), AlopexNotificationListenerService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        Log.i(TAG, "rebind: Enabled");
        rebinding = false;
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.i(TAG, "onListenerDisconnected: ");
        active = false;
        if (!rebinding) {
            Notification notification = new Notification.Builder(this, CHANNEL_ONE_ID)
                    .setAutoCancel(true)
                    .setContentTitle("Listener Disconnected")
                    .setContentText("request Rebind now")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .build();
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
            managerCompat.notify(1001, notification);
            rebind();
        }
    }

    public void cancelNotification(StatusBarNotification sbn) {
        cancelNotification(sbn.getKey());
    }
}