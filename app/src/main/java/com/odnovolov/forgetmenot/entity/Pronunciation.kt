package com.odnovolov.forgetmenot.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Pronunciation(
    val id: Int = 0,
    val name: String,
    val questionLanguage: Locale? = null,
    val questionAutoSpeak: Boolean = false,
    val answerLanguage: Locale? = null,
    val answerAutoSpeak: Boolean = false
) : Parcelable