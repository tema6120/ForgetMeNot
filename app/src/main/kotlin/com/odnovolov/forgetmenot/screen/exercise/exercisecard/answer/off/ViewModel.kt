package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.off

import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.mapToOne
import com.odnovolov.forgetmenot.exercise.exercisecards.ExerciseCardViewModelQueries
import kotlinx.coroutines.flow.Flow

class AnswerOffTestViewModel(id: Long) {
    private val queries: ExerciseCardViewModelQueries = database.exerciseCardViewModelQueries

    val answer: Flow<String> = queries
        .getAnswer(id)
        .asFlow()
        .mapToOne()

    val isAnswered: Flow<Boolean> = queries
        .isAnswered(id)
        .asFlow()
        .mapToOne()

    val isLearned: Flow<Boolean> = queries
        .isLearned(id)
        .asFlow()
        .mapToOne()
}