package com.afoxxvi.alopex;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.widget.Toast;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.afoxxvi.alopex.util.FoxFiles;

import org.apache.commons.io.input.ReversedLinesFileReader;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AlopexView extends BaseObservable {
    public static final String TAG = "AlopexView";

    private static final int LOG_MAX_DISPLAY = 20;
    private static final String[] LOG_ARRAY = new String[LOG_MAX_DISPLAY];
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter MONTH_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss");

    public static boolean active = false;
    public static Handler handlerNotification;

    private static AlopexView inst;
    private static LocalDateTime lastConnect = null;
    private static LocalDateTime lastDisconnect = null;
    private static int logIndex = 0;
    private static PrintStream logStream;
    private static SharedPreferences properties;

    public static AlopexView getInstance() {
        if (inst == null) {
            inst = new AlopexView();
        }
        return inst;
    }

    public static String getDateTimeString() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }

    private static Toast toast = null;

    public static void showToast(Context context, CharSequence text, int duration) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public static void showToastShort(Context context, CharSequence text) {
        showToast(context, text, Toast.LENGTH_SHORT);
    }

    public static void showToastLong(Context context, CharSequence text) {
        showToast(context, text, Toast.LENGTH_LONG);
    }

    private AlopexView() {
    }

    public static SharedPreferences getProperties() {
        return properties;
    }

    @Bindable
    public String getLogStrings() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < LOG_MAX_DISPLAY; i++) {
            String next = LOG_ARRAY[(logIndex - 1 - i + LOG_MAX_DISPLAY) % LOG_MAX_DISPLAY];
            if (next == null) {
                break;
            }
            if (i > 0) {
                builder.append('\n');
            }
            builder.append(next);
        }
        return builder.toString();
    }

    public static void init(Context context) {
        File logFile = new File(context.getFilesDir(), "logs.log");
        try {
            ReversedLinesFileReader reader = new ReversedLinesFileReader(logFile, StandardCharsets.UTF_8);
            List<String> lines = reader.readLines(LOG_MAX_DISPLAY);
            for (int i = 0; i < lines.size(); i++) {
                LOG_ARRAY[i] = lines.get(lines.size() - 1 - i);
            }
            logIndex = lines.size() % LOG_MAX_DISPLAY;
        } catch (IOException e) {
            e.printStackTrace();
        }
        logStream = FoxFiles.getOutputStream(context, "logs.log");
        properties = context.getSharedPreferences("properties", Context.MODE_PRIVATE);
        lastConnect = LocalDateTime.parse(properties.getString("lastConnect", "2022-01-01 12:00:00"), DATE_TIME_FORMATTER);
        lastDisconnect = LocalDateTime.parse(properties.getString("lastDisconnect", "2022-01-01 12:00:00"), DATE_TIME_FORMATTER);
    }

    public static void log(String content) {
        String str = "[" + MONTH_TIME_FORMATTER.format(LocalDateTime.now()) + "] " + content;
        LOG_ARRAY[logIndex] = str;
        logIndex = (logIndex + 1) % LOG_MAX_DISPLAY;
        logStream.println(str);
    }

    public static void notifyConnect() {
        log("connect");
        lastConnect = LocalDateTime.now();
        properties.edit().putString("lastConnect", DATE_TIME_FORMATTER.format(lastConnect)).apply();
        getInstance().notifyChange();
    }

    public static void notifyDisconnect() {
        log("disconnect");
        lastDisconnect = LocalDateTime.now();
        properties.edit().putString("lastDisconnect", DATE_TIME_FORMATTER.format(lastDisconnect)).apply();
        getInstance().notifyChange();
    }

    @Bindable
    public String getLastConnectStr() {
        return lastConnect == null ? "No connect event" : DATE_TIME_FORMATTER.format(lastConnect);
    }

    @Bindable
    public String getLastDisconnectStr() {
        return lastDisconnect == null ? "No disconnect event" : DATE_TIME_FORMATTER.format(lastDisconnect);
    }
}
