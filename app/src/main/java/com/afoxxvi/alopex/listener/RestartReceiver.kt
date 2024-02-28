package com.afoxxvi.alopex.listener

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.afoxxvi.alopex.Alopex
import com.afoxxvi.alopex.MainActivity
import com.afoxxvi.alopex.service.AlopexNotificationListenerService

class RestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(Alopex.TAG, "onReceive: active = " + Alopex.active)
        if (!Alopex.active) {
            val intent1 = Intent(context, MainActivity::class.java)
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent1)
        }
        if (!AlopexNotificationListenerService.active) {
            context.startService(Intent(context, AlopexNotificationListenerService::class.java))
        }
    }
}