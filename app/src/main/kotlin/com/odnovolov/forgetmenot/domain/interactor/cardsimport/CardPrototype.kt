package com.odnovolov.forgetmenot.domain.interactor.cardsimport

import com.odnovolov.forgetmenot.domain.entity.Card
import kotlinx.serialization.Serializable

@Serializable
data class CardPrototype(
    val id: Long,
    val question: String,
    val answer: String,
    val isSelected: Boolean
) {
    fun toCard() = Card(
        id,
        question,
        answer
    )
}