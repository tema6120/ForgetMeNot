package com.odnovolov.forgetmenot.presentation.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DeckPreview(
    val deckId: Int,
    val deckName: String,
    val passedLaps: Int,
    val progress: Progress
) : Parcelable {

    @Parcelize
    data class Progress(
        val learned: Int,
        val total: Int
    ): Parcelable {
        override fun toString(): String {
            return "$learned/$total"
        }
    }
}