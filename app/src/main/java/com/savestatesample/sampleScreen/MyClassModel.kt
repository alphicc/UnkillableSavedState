package com.savestatesample.sampleScreen

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MyClassModel(
    val myValue: Int
): Parcelable