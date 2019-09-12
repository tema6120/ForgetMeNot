package com.odnovolov.forgetmenot.exercise

import com.odnovolov.forgetmenot.common.database.*
import com.odnovolov.forgetmenot.common.entity.TestMethod
import kotlinx.coroutines.flow.Flow

class ExerciseViewModel {
    private val queries: ExerciseViewModelQueries = database.exerciseViewModelQueries

    val testMethod: TestMethod by lazy {
        val databaseValue = queries.getTestMethod().executeAsOne()
        testMethodAdapter.decode(databaseValue)
    }

    val cardsIdsAtStart: List<Long> by lazy { queries.getCardIdsInExercise().executeAsList() }

    val cardIds: Flow<List<Long>> = queries
        .getCardIdsInExercise()
        .asFlow()
        .mapToList()

    val isCurrentCardLearned: Flow<Boolean?> = queries
        .isCurrentCardLearned()
        .asFlow()
        .mapToOneOrNull()
}