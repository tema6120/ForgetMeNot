package com.odnovolov.forgetmenot.domain.feature.exercise

import com.odnovolov.forgetmenot.domain.entity.Card

data class ExerciseCard(
    val card: Card,
    var isAnswered: Boolean = false
)