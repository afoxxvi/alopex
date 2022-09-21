package com.afoxxvi.alopex.listener;

import static com.afoxxvi.alopex.AlopexView.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.afoxxvi.alopex.AlopexView;
import com.afoxxvi.alopex.MainActivity;

public class RestartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: active = " + AlopexView.active);
        if (!AlopexView.active) {
            Intent intent1 = new Intent(context, MainActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
        }
    }
}