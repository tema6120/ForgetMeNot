package com.odnovolov.forgetmenot.exercise.exercisecard.withouttest

import com.odnovolov.forgetmenot.common.database.asBoolean
import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.mapToOne
import com.odnovolov.forgetmenot.exercise.exercisecard.ExerciseCardViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseCardWithoutTextViewModel(cardId: Long) : ExerciseCardViewModel(cardId) {
    val isAnswered: Flow<Boolean> = queries
        .isAnswered(cardId)
        .asFlow()
        .mapToOne()
        .map { databaseValue: Long -> databaseValue.asBoolean() }
}