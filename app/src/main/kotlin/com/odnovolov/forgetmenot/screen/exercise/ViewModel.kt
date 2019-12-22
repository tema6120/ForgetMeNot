package com.odnovolov.forgetmenot.screen.exercise

import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.mapToList
import com.odnovolov.forgetmenot.common.database.mapToOneOrNull
import com.odnovolov.forgetmenot.exercise.ExerciseViewModelQueries
import com.odnovolov.forgetmenot.exercise.IntervalItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseViewModel {
    private val queries: ExerciseViewModelQueries = database.exerciseViewModelQueries

    val exerciseCardsIdsAtStart: List<Long> = queries
        .getAllExerciseCardIds()
        .executeAsList()

    val exerciseCardIds: Flow<List<Long>> = queries
        .getAllExerciseCardIds()
        .asFlow()
        .mapToList()

    val isCurrentExerciseCardLearned: Flow<Boolean?> = queries
        .isCurrentExerciseCardLearned()
        .asFlow()
        .mapToOneOrNull()
        .map { it?.isLearned }

    val levelOfKnowledgeForCurrentCard: Flow<Int?> = queries
        .getLevelOfKnowledgeForCurrentCard()
        .asFlow()
        .mapToOneOrNull()

    val intervalItems: Flow<List<IntervalItem>> = queries.intervalItem().asFlow().mapToList()
}