package com.odnovolov.forgetmenot.domain.interactor.deckcreator

import kotlinx.serialization.Serializable

@Serializable
data class CardPrototype(
    val question: String,
    val answer: String
)