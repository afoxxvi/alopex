package com.afoxxvi.alopex.component.notify

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NotifyStorage(context: Context?, name: String?) : SQLiteOpenHelper(context, name, null, VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_PACKAGE)
        db.execSQL(CREATE_NOTIFY)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    companion object {
        private const val VERSION = 1
        private const val CREATE_PACKAGE = "" +
                "create table Package(" +
                "id integer primary key autoincrement," +
                "package text" +
                ");"
        private const val CREATE_NOTIFY = "" +
                "create table Notify(" +
                "package integer," +
                "title text," +
                "content text," +
                "time datetime" +
                ");"
    }
}