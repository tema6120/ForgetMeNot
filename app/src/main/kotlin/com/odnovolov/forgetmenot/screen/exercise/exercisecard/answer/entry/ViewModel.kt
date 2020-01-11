package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.entry

import com.odnovolov.forgetmenot.common.database.*
import com.odnovolov.forgetmenot.screen.exercise.exercisecards.ExerciseCardViewModelQueries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AnswerEntryTestViewModel(id: Long) {
    private val queries: ExerciseCardViewModelQueries = database.exerciseCardViewModelQueries

    val isAnswered: Flow<Boolean> = queries
        .isAnswered(id)
        .asFlow()
        .mapToOneNotNull()

    val hint: Flow<String?> = queries
        .getHint(id)
        .asFlow()
        .mapToOne()
        .map { it.hint }

    val correctAnswer: Flow<String> = queries
        .getAnswer(id)
        .asFlow()
        .mapToOneNotNull()

    val wrongAnswer: Flow<String?> = queries
        .getWrongAnswer(id)
        .asFlow()
        .mapToOneNotNull()
        .map { it.wrongAnswer }

    val isLearned: Flow<Boolean> = queries
        .isLearned(id)
        .asFlow()
        .mapToOneNotNull()
}