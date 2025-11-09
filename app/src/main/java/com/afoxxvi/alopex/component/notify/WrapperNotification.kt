package com.afoxxvi.alopex.component.notify

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.appcompat.R
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WrapperNotification : BaseObservable, Parcelable {
    @get:Bindable
    val title: String?

    @get:Bindable
    val text: String?
    val time: LocalDateTime

    @get:Bindable
    var isFiltered: Boolean

    constructor(title: String?, text: String?, time: LocalDateTime) {
        this.title = title
        this.text = text
        this.time = time
        isFiltered = false
    }

    private constructor(parcel: Parcel) {
        title = parcel.readString()
        text = parcel.readString()
        time = LocalDateTime.parse(parcel.readString())
        isFiltered = parcel.readByte().toInt() != 0
    }

    @get:Bindable
    val color: Int
        get() = if (isFiltered) {
            R.attr.colorPrimary
        } else {
            android.R.attr.textColorSecondary
        }

    @get:Bindable
    val dateText: String
        get() {
            val today = LocalDate.now()
            return if (time.toLocalDate().isEqual(today)) {
                time.format(DateTimeFormatter.ofPattern("HH:mm"))
            } else time.format(DateTimeFormatter.ofPattern("MM-dd"))
        }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(title)
        dest.writeString(text)
        dest.writeString(time.toString())
        dest.writeByte((if (isFiltered) 1 else 0).toByte())
    }

    companion object {
        @JvmField
        val CREATOR: Creator<WrapperNotification> = object : Creator<WrapperNotification> {
            override fun createFromParcel(parcel: Parcel): WrapperNotification {
                return WrapperNotification(parcel)
            }

            override fun newArray(size: Int): Array<WrapperNotification?> {
                return arrayOfNulls(size)
            }
        }
    }
}