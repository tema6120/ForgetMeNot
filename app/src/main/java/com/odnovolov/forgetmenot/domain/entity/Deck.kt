package com.odnovolov.forgetmenot.domain.entity

data class Deck(
    val id: Int = 0, // we delegate generation to repository
    val name: String,
    val cards: List<Card>
)