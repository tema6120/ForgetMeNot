package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.off

import com.odnovolov.forgetmenot.common.database.asBoolean
import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.mapToOne
import com.odnovolov.forgetmenot.exercise.exercisecards.ExerciseCardViewModelQueries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
        .map { databaseValue: Long -> databaseValue.asBoolean() }

    val isLearned: Flow<Boolean> = queries
        .isLearned(id)
        .asFlow()
        .mapToOne()
}