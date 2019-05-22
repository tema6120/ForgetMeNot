package com.odnovolov.forgetmenot.domain.entity

data class Card(
    val id: Int = 0, // we delegate generation to repository
    val ordinal: Int,
    val question: String,
    val answer: String
)