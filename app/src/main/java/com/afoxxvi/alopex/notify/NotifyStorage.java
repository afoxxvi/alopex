package com.afoxxvi.alopex.notify;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class NotifyStorage extends SQLiteOpenHelper {
    private static final int VERSION = 1;

    public NotifyStorage(@Nullable Context context, @Nullable String name) {
        super(context, name, null, VERSION);
    }

    private static final String CREATE_PACKAGE = "" +
            "create table Package(" +
            "id integer primary key autoincrement," +
            "package text" +
            ");";

    private static final String CREATE_NOTIFY = "" +
            "create table Notify(" +
            "package integer," +
            "title text," +
            "content text," +
            "time datetime" +
            ");";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PACKAGE);
        db.execSQL(CREATE_NOTIFY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
