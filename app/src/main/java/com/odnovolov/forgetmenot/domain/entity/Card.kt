package com.odnovolov.forgetmenot.domain.entity

interface Card {
    val id: Long
    val question: String
    val answer: String
}