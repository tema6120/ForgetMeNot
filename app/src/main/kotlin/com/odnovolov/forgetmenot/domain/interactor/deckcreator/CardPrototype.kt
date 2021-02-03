package com.odnovolov.forgetmenot.domain.interactor.deckcreator

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.generateId
import kotlinx.serialization.Serializable

@Serializable
data class CardPrototype(
    val question: String,
    val answer: String
) {
    fun toCard() = Card(
        id = generateId(),
        question,
        answer
    )
}