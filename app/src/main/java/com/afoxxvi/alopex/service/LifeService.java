package com.afoxxvi.alopex.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.afoxxvi.alopex.util.FoxTools;

import java.util.concurrent.TimeUnit;

public class LifeService extends Service {
    public LifeService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FoxTools.getSingleExecutorService()
                .scheduleWithFixedDelay(() -> {
                }, 3, 60, TimeUnit.SECONDS);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new Binder() {

    };
}