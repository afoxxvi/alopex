package com.afoxxvi.alopex.service;

import static com.afoxxvi.alopex.AlopexView.TAG;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

public class DaemonService extends JobService {
    private static final int JOB_ID = 0x91ec499f;

    public DaemonService() {
    }

    public static void schedule(Context context) {
        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, new ComponentName(context, DaemonService.class))
                .setMinimumLatency(9000)
                .setOverrideDeadline(10000)
                .setRequiresCharging(false)
                .setPersisted(true)
                .build();
        JobScheduler scheduler = context.getSystemService(JobScheduler.class);
        int res = scheduler.schedule(jobInfo);
        Log.i(TAG, "schedule: res = " + res);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        if (!AlopexNotificationListenerService.active) {
            PackageManager pm = getPackageManager();
            pm.setComponentEnabledSetting(new ComponentName(getApplicationContext(), AlopexNotificationListenerService.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(new ComponentName(getApplicationContext(), AlopexNotificationListenerService.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }
        Log.i(TAG, "onStartJob: live = " + AlopexNotificationListenerService.active);
        //schedule(this);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}