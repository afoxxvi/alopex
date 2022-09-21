package com.afoxxvi.alopex.notify;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notify extends BaseObservable implements Parcelable {
    private final String title;
    private final String text;
    private final LocalDateTime time;
    private boolean filtered;

    public Notify(String title, String text, LocalDateTime time) {
        this.title = title;
        this.text = text;
        this.time = time;
        this.filtered = false;
    }

    protected Notify(Parcel in) {
        title = in.readString();
        text = in.readString();
        time = LocalDateTime.parse(in.readString());
        filtered = in.readByte() != 0;
    }

    public static final Creator<Notify> CREATOR = new Creator<Notify>() {
        @Override
        public Notify createFromParcel(Parcel in) {
            return new Notify(in);
        }

        @Override
        public Notify[] newArray(int size) {
            return new Notify[size];
        }
    };

    @Bindable
    public String getTitle() {
        return title;
    }

    @Bindable
    public String getText() {
        return text;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setFiltered(boolean filtered) {
        this.filtered = filtered;
    }

    @Bindable
    public int getColor() {
        if (isFiltered()) {
            return androidx.appcompat.R.attr.colorPrimary;
        } else {
            return android.R.attr.textColorSecondary;
        }
    }

    @Bindable
    public boolean isFiltered() {
        return filtered;
    }

    @Bindable
    public String getDateText() {
        LocalDate today = LocalDate.now();
        if (time.toLocalDate().isEqual(today)) {
            return time.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        return time.format(DateTimeFormatter.ofPattern("MM-dd"));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(text);
        dest.writeString(time.toString());
        dest.writeByte((byte) (filtered ? 1 : 0));
    }
}
