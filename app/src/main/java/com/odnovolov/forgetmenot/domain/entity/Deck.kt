package com.odnovolov.forgetmenot.domain.entity

import java.util.*

data class Deck(
    val id: Int = 0,
    val name: String,
    val cards: List<Card>,
    val createdAt: Calendar = Calendar.getInstance(),
    val lastOpenedAt: Calendar? = null
)