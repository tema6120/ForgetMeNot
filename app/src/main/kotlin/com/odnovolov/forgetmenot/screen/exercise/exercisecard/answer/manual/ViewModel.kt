package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.manual

import com.odnovolov.forgetmenot.common.database.*
import com.odnovolov.forgetmenot.screen.exercise.exercisecards.ExerciseCardViewModelQueries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AnswerManualTestViewModel(id: Long) {
    private val queries: ExerciseCardViewModelQueries = database.exerciseCardViewModelQueries

    val answer: Flow<String> = queries
        .getAnswer(id)
        .asFlow()
        .mapToOneNotNull()

    val isAnswerCorrect: Flow<Boolean?> = queries
        .isAnswerCorrect(id)
        .asFlow()
        .mapToOneNotNull()
        .map { it.isAnswerCorrect }

    val isLearned: Flow<Boolean> = queries
        .isLearned(id)
        .asFlow()
        .mapToOneNotNull()
}