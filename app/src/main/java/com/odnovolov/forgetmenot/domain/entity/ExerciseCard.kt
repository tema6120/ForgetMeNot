package com.odnovolov.forgetmenot.domain.entity

data class ExerciseCard(
        val id: Int = 0,
        val card: Card,
        var isAnswered: Boolean = false
)