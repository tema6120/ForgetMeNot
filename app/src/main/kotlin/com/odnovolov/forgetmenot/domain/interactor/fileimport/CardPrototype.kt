package com.odnovolov.forgetmenot.domain.interactor.fileimport

import com.odnovolov.forgetmenot.domain.entity.Card

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