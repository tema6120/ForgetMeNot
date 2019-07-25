package com.odnovolov.forgetmenot.entity

import java.util.*

data class Deck(
    val id: Int = 0,
    val name: String,
    val cards: List<Card>,
    val createdAt: Calendar = Calendar.getInstance(),
    val lastOpenedAt: Calendar? = null
)