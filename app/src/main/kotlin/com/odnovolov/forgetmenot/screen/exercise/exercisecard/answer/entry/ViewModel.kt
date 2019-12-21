package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.entry

import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.mapToOne
import com.odnovolov.forgetmenot.exercise.exercisecards.ExerciseCardViewModelQueries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AnswerEntryTestViewModel(id: Long) {
    private val queries: ExerciseCardViewModelQueries = database.exerciseCardViewModelQueries

    val isAnswered: Flow<Boolean> = queries
        .isAnswered(id)
        .asFlow()
        .mapToOne()

    val correctAnswer: Flow<String> = queries
        .getAnswer(id)
        .asFlow()
        .mapToOne()

    val wrongAnswer: Flow<String?> = queries
        .getWrongAnswer(id)
        .asFlow()
        .mapToOne()
        .map { it.wrongAnswer }

    val isLearned: Flow<Boolean> = queries
        .isLearned(id)
        .asFlow()
        .mapToOne()
}