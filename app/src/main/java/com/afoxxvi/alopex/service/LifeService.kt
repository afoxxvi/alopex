package com.afoxxvi.alopex.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.afoxxvi.alopex.util.FoxTools.singleExecutorService
import java.util.concurrent.TimeUnit

class LifeService : Service() {
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        singleExecutorService.scheduleWithFixedDelay({}, 3, 60, TimeUnit.SECONDS)
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    private val mBinder: IBinder = object : Binder() {}
}