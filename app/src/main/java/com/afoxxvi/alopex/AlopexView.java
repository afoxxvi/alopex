package com.afoxxvi.alopex;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.StringRes;

public class AlopexView {
    public static boolean active = false;
    public static Handler handlerNotification;
    public static Handler handlerMain;
    public static final String TAG = "AlopexView";

    private static Toast toast = null;

    public static void showToast(Context context, CharSequence text, int duration) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public static void showToast(Context context, @StringRes int resId, int duration) {
        showToast(context, context.getResources().getText(resId), duration);
    }
}
