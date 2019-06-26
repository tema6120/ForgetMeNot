package com.odnovolov.forgetmenot.domain.entity

data class Deck(
    val id: Int = 0,
    val name: String,
    val cards: List<Card>
)