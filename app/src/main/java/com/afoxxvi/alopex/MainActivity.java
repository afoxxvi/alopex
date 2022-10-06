package com.afoxxvi.alopex;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
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
import com.afoxxvi.alopex.service.LifeService;
import com.afoxxvi.alopex.ui.fragment.FilterFragment;
import com.afoxxvi.alopex.ui.fragment.InformationFragment;
import com.afoxxvi.alopex.ui.fragment.NotificationFragment;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    public static final String CHANNEL_ONE_ID = "ALOPEX_CHANNEL_ONE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AlopexView.active = true;
        AlopexView.init(this);
        setSupportActionBar(binding.toolbar);
        if (!isNotificationListenerEnabled()) {
            openNotificationListenerSettings();
        } else if (!AlopexNotificationListenerService.active) {
            startService(new Intent(this, AlopexNotificationListenerService.class));
            startService(new Intent(this, LifeService.class));
        }
        AlopexFilterManager.getInstance().init(this);
        NotifyManager.getInstance().init(this);
        final String[] tabs = new String[]{"Notification", "Filter", "Information"};
        binding.pager.setOffscreenPageLimit(3);
        binding.pager.setAdapter(new FragmentStateAdapter(getSupportFragmentManager(), getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0:
                        return NotificationFragment.newInstance();
                    case 1:
                        return FilterFragment.newInstance();
                    case 2:
                        return InformationFragment.newInstance();
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

    private boolean isNotificationListenerEnabled() {
        return NotificationManagerCompat.getEnabledListenerPackages(this).contains(this.getPackageName());
    }

    private void openNotificationListenerSettings() {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        startActivity(intent);
    }
}