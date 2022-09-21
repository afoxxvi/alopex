package com.afoxxvi.alopex.notify;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotifyManager {
    private final List<NotifyGroup> notifyGroupList;
    private final Map<String, Integer> packageIdMap;
    private NotifyStorage notifyStorage;
    private static final int INITIAL_MAX_NOTIFY_COUNT = 30;
    private static final int MAX_NOTIFY_COUNT = 30;
    private static final int INITIAL_MAX_TOTAL_NOTIFY_COUNT = 200;

    private static NotifyManager inst;

    public static NotifyManager getInstance() {
        if (inst == null) {
            inst = new NotifyManager();
        }
        return inst;
    }

    private NotifyManager() {
        notifyGroupList = new ArrayList<>();
        packageIdMap = new HashMap<>(32);
    }

    public int newNotify(Context context, String pkg, Notify notify) {
        Integer id = packageIdMap.get(pkg);
        SQLiteDatabase db = notifyStorage.getReadableDatabase();
        int fromIndex;
        if (id == null) {
            SQLiteStatement stmt = db.compileStatement("insert into Package(package) values(?)");
            stmt.bindString(1, pkg);
            id = Math.toIntExact(stmt.executeInsert());
            if (id != -1) {
                packageIdMap.put(pkg, id);
                NotifyGroup notifyGroup = new NotifyGroup(context, pkg, id);
                notifyGroup.getNotifyList().add(0, notify);
                notifyGroupList.add(0, notifyGroup);
            }
            fromIndex = id == -1 ? -1 : notifyGroupList.size();
        } else {
            int i;
            for (i = 0; i < notifyGroupList.size(); i++) {
                NotifyGroup notifyGroup = notifyGroupList.get(i);
                if (notifyGroup.getPackageId() == id) {
                    notifyGroup.getNotifyList().add(0, notify);
                    if (notifyGroup.getNotifyCount() > MAX_NOTIFY_COUNT) {
                        notifyGroup.getNotifyList().subList(MAX_NOTIFY_COUNT, notifyGroup.getNotifyCount()).clear();
                    }
                    break;
                }
            }
            NotifyGroup notifyGroup;
            if (i < notifyGroupList.size()) {
                notifyGroup = notifyGroupList.remove(i);
            } else {
                notifyGroup = new NotifyGroup(context, pkg, id);
            }
            notifyGroupList.add(0, notifyGroup);
            notifyGroup.notifyChange();
            fromIndex = i;
        }
        if (id != -1) {
            SQLiteStatement stmt = db.compileStatement("insert into Notify(package, title, content, time) values(?,?,?,?)");
            stmt.bindLong(1, id);
            stmt.bindString(2, notify.getTitle());
            stmt.bindString(3, notify.getText());
            stmt.bindString(4, notify.getTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            stmt.executeInsert();
        }
        return fromIndex;
    }

    public List<NotifyGroup> getNotifyGroupList() {
        return notifyGroupList;
    }

    public void requestMore(NotifyGroup group, int increment) {
        SQLiteDatabase db = notifyStorage.getReadableDatabase();
        Cursor cursor = db.rawQuery("select title, content, time from Notify where package = ? and time < ? order by time desc limit ?",
                new String[]{
                        String.valueOf(group.getPackageId()),
                        group.getEarliestNotifyTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        String.valueOf(increment)
                });
        if (cursor.moveToFirst()) {
            String title;
            String text;
            LocalDateTime time;
            do {
                title = cursor.getString(0);
                text = cursor.getString(1);
                time = LocalDateTime.parse(cursor.getString(2), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                Notify notify = new Notify(title, text, time);
                group.getNotifyList().add(notify);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    public void init(Context context) {
        notifyStorage = new NotifyStorage(context, "notify.db");
        SQLiteDatabase db = notifyStorage.getReadableDatabase();
        Cursor cursor = db.rawQuery("select id, package from Package order by id", new String[0]);
        Map<Integer, Integer> indexMap = new HashMap<>(cursor.getCount());
        int index = 0;
        if (cursor.moveToFirst()) {
            int id;
            String pkg;
            do {
                id = cursor.getInt(0);
                pkg = cursor.getString(1);
                notifyGroupList.add(new NotifyGroup(context, pkg, id));
                packageIdMap.put(pkg, id);
                indexMap.put(id, index);
                index++;
            } while (cursor.moveToNext());
        }
        cursor.close();

        SQLiteStatement stmt = db.compileStatement("delete from Notify where time < ?");
        stmt.bindString(1, LocalDateTime.now().minusDays(14).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        stmt.executeUpdateDelete();

        cursor = db.rawQuery("select package, title, content, time from Notify order by time desc limit ?",
                new String[]{String.valueOf(INITIAL_MAX_TOTAL_NOTIFY_COUNT)});
        if (cursor.moveToFirst()) {
            int pkg;
            String title;
            String text;
            LocalDateTime time;
            do {
                pkg = cursor.getInt(0);
                title = cursor.getString(1);
                text = cursor.getString(2);
                time = LocalDateTime.parse(cursor.getString(3), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                Notify notify = new Notify(title, text, time);
                Integer i = indexMap.get(pkg);
                if (i != null && notifyGroupList.get(i).getNotifyCount() < INITIAL_MAX_NOTIFY_COUNT) {
                    notifyGroupList.get(i).getNotifyList().add(notify);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        notifyGroupList.sort((g1, g2) -> -g1.getLatestTime().compareTo(g2.getLatestTime()));
        db.close();
    }
}
