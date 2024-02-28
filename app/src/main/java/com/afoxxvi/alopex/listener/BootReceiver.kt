package com.afoxxvi.alopex.listener

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.afoxxvi.alopex.MainActivity

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (ACTION_BOOT_COMPLETED == intent.action) {
            val intent1 = Intent(context, MainActivity::class.java)
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent1)
        }
    }

    companion object {
        private const val ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED"
    }
}