package com.example.cookper

import android.os.Parcel
import android.os.Parcelable

data class Cook (
    var num: Int = 0,
    var cooking: String = "",
    var food: String = "",
    var timer: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(num)
        parcel.writeString(cooking)
        parcel.writeString(food)
        parcel.writeString(timer)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Cook> {
        override fun createFromParcel(parcel: Parcel): Cook {
            return Cook(parcel)
        }

        override fun newArray(size: Int): Array<Cook?> {
            return arrayOfNulls(size)
        }
    }
}