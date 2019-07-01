package com.odnovolov.forgetmenot.presentation.entity

import android.os.Parcelable
import com.odnovolov.forgetmenot.domain.entity.Card
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CardViewEntity(
    val id: Int = 0,
    val ordinal: Int,
    val question: String,
    val answer: String,
    val lap: Int = 0,
    val isLearned: Boolean = false
) : Parcelable {

    fun toCard() = Card(
        id,
        ordinal,
        question,
        answer,
        lap,
        isLearned
    )

    companion object {
        fun fromCard(card: Card) = CardViewEntity(
            card.id,
            card.ordinal,
            card.question,
            card.answer,
            card.lap,
            card.isLearned
        )
    }
}