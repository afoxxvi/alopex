package com.afoxxvi.alopex.component.notify;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotifyGroup extends BaseObservable {
    private final String packageName;
    private String appName;
    private Drawable appIcon;
    private final int packageId;
    private final List<Notify> notifyList;

    public NotifyGroup(Context context, String packageName, int packageId) {
        this.packageName = packageName;
        this.packageId = packageId;
        this.notifyList = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo info = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA);
            this.appName = info.applicationInfo.loadLabel(packageManager).toString();
            this.appIcon = info.applicationInfo.loadIcon(packageManager);
        } catch (PackageManager.NameNotFoundException e) {
            this.appName = null;
            this.appIcon = null;
        }
    }

    @Bindable
    public String getPackageName() {
        return packageName;
    }

    @Bindable
    public String getAppName() {
        return appName == null ? getPackageName() : appName;
    }

    @Bindable
    public Drawable getAppIcon() {
        return appIcon;
    }

    public int getPackageId() {
        return packageId;
    }

    @Bindable
    public String getLatestTitle() {
        if (!notifyList.isEmpty()) {
            return notifyList.get(0).getTitle();
        }
        return "<Empty>";
    }

    @Bindable
    public String getLatestText() {
        if (!notifyList.isEmpty()) {
            return notifyList.get(0).getText();
        }
        return "<No notification recently>";
    }

    @Bindable
    public String getLatestTimeText() {
        if (!notifyList.isEmpty()) {
            return notifyList.get(0).getDateText();
        }
        return "";
    }

    @Bindable
    public Integer getNotifyCount() {
        return notifyList.size();
    }

    public LocalDateTime getLatestTime() {
        if (!notifyList.isEmpty()) {
            return notifyList.get(0).getTime();
        }
        return LocalDateTime.MIN;
    }

    public LocalDateTime getEarliestNotifyTime() {
        if (!notifyList.isEmpty()) {
            return notifyList.get(notifyList.size() - 1).getTime();
        }
        return LocalDateTime.now();
    }

    public List<Notify> getNotifyList() {
        return notifyList;
    }
}
