package com.odnovolov.forgetmenot.domain.entity

data class Card(
    val id: Int = 0,
    val ordinal: Int,
    val question: String,
    val answer: String,
    val lap: Int = 0
)