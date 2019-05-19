package com.odnovolov.forgetmenot.domain.entity

interface Deck {
    val id: Long
    val cards: List<Card>
}