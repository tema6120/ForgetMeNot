package com.odnovolov.forgetmenot.domain.feature.exercise

import com.odnovolov.forgetmenot.domain.entity.Card

data class ExerciseCard(
        val id: Int = 0, // we delegate generation to repository
        val card: Card,
        var isAnswered: Boolean = false
)