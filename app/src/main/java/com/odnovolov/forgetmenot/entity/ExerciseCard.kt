package com.odnovolov.forgetmenot.entity

data class ExerciseCard(
    val id: Int = 0,
    val card: Card,
    var isAnswered: Boolean = false
)