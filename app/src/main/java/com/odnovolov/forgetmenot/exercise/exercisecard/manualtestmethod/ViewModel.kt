package com.odnovolov.forgetmenot.exercise.exercisecard.manualtestmethod

import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.mapToOne
import com.odnovolov.forgetmenot.exercise.exercisecard.ExerciseCardViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseCardManualTestMethodViewModel(cardId: Long) : ExerciseCardViewModel(cardId) {
    val isAnswerCorrect: Flow<Boolean?> = queries
        .isAnswerCorrect(cardId)
        .asFlow()
        .mapToOne()
        .map { it.isAnswerCorrect }
}