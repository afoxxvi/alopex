package com.afoxxvi.alopex;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.afoxxvi.alopex.component.filter.AlopexFilterManager;
import com.afoxxvi.alopex.component.notify.NotifyManager;
import com.afoxxvi.alopex.databinding.ActivityMainBinding;
import com.afoxxvi.alopex.listener.RestartReceiver;
import com.afoxxvi.alopex.service.AlopexNotificationListenerService;
import com.afoxxvi.alopex.ui.fragment.FilterFragment;
import com.afoxxvi.alopex.ui.fragment.NotificationFragment;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    public static final String CHANNEL_ONE_ID = "ALOPEX_CHANNEL_ONE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AlopexView.active = true;
        setSupportActionBar(binding.toolbar);
        if (!isNotificationListenerEnabled()) {
            openNotificationListenerSettings();
        } else if (!AlopexNotificationListenerService.active) {
            PackageManager pm = getPackageManager();
            pm.setComponentEnabledSetting(new ComponentName(getApplicationContext(), AlopexNotificationListenerService.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(new ComponentName(getApplicationContext(), AlopexNotificationListenerService.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }
        AlopexFilterManager.getInstance().init(this);
        NotifyManager.getInstance().init(this);
        final String[] tabs = new String[]{"Notification", "Filter"};
        binding.pager.setOffscreenPageLimit(2);
        binding.pager.setAdapter(new FragmentStateAdapter(getSupportFragmentManager(), getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0:
                        return NotificationFragment.newInstance();
                    case 1:
                        return FilterFragment.newInstance();
                    default:
                }
                return new Fragment();
            }

            @Override
            public int getItemCount() {
                return tabs.length;
            }
        });
        TabLayoutMediator mediator = new TabLayoutMediator(binding.tabs, binding.pager, (tab, position) -> {
            tab.setText(tabs[position]);
        });
        mediator.attach();
        NotificationChannel channel = new NotificationChannel(CHANNEL_ONE_ID, "Alopex Channel 1", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        AlarmManager alarmManager = getSystemService(AlarmManager.class);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, 60000,
                PendingIntent.getBroadcast(this, 1, new Intent(this, RestartReceiver.class), PendingIntent.FLAG_IMMUTABLE));

        AlopexView.handlerMain = handler;
        ScheduledExecutorService saveTask = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
            final ThreadFactory factory = Executors.defaultThreadFactory();

            @Override
            public Thread newThread(Runnable r) {
                return factory.newThread(r);
            }
        });
        saveTask.scheduleWithFixedDelay(() -> Message.obtain(handler, WHAT_SAVE_FILTER).sendToTarget(), 3, 300, TimeUnit.SECONDS);
        //DaemonService.schedule(this);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
    }

    @Override
    protected void onDestroy() {
        AlopexFilterManager.getInstance().save(this);
        AlopexView.active = false;
        super.onDestroy();
    }

    public static final int WHAT_SAVE_FILTER = 1;

    private final Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == WHAT_SAVE_FILTER) {
                AlopexFilterManager.getInstance().save(MainActivity.this);
            }
        }
    };

    private boolean isNotificationListenerEnabled() {
        return NotificationManagerCompat.getEnabledListenerPackages(this).contains(this.getPackageName());
    }

    private void openNotificationListenerSettings() {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        startActivity(intent);
    }
}