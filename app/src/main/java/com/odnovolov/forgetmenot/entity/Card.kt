package com.odnovolov.forgetmenot.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Card(
    val id: Int = 0,
    val ordinal: Int,
    val question: String,
    val answer: String,
    val lap: Int = 0,
    val isLearned: Boolean = false
) : Parcelable