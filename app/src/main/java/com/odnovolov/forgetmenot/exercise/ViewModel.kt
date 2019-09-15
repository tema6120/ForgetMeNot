package com.odnovolov.forgetmenot.exercise

import com.odnovolov.forgetmenot.common.database.*
import com.odnovolov.forgetmenot.common.entity.TestMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseViewModel {
    private val queries: ExerciseViewModelQueries = database.exerciseViewModelQueries

    val testMethod: TestMethod by lazy {
        val databaseValue = queries.getTestMethod().executeAsOne()
        testMethodAdapter.decode(databaseValue)
    }

    val exerciseCardsIdsAtStart: List<Long> by lazy {
        queries.getAllExerciseCardIds().executeAsList()
    }

    val exerciseCardIds: Flow<List<Long>> = queries
        .getAllExerciseCardIds()
        .asFlow()
        .mapToList()

    val isCurrentExerciseCardLearned: Flow<Boolean?> = queries
        .isCurrentExerciseCardLearned()
        .asFlow()
        .mapToOneOrNull()
        .map { it?.isLearned }

    val levelOfKnowledgeForCurrentCard: Flow<Int> = queries
        .getLevelOfKnowledgeForCurrentCard()
        .asFlow()
        .mapToOne()
}