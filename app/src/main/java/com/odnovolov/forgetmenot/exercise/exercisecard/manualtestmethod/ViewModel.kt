package com.odnovolov.forgetmenot.exercise.exercisecard.manualtestmethod

import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.mapToOneOrNull
import com.odnovolov.forgetmenot.exercise.exercisecard.ExerciseCardViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseCardManualTestMethodViewModel(id: Long) : ExerciseCardViewModel(id) {
    val isAnswerCorrect: Flow<Boolean?> = queries
        .isAnswerCorrect(id)
        .asFlow()
        .mapToOneOrNull()
        .map { it?.isAnswerCorrect }
}