package com.odnovolov.forgetmenot.screen.exercise

import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.mapToList
import com.odnovolov.forgetmenot.common.database.mapToOneOrNull
import kotlinx.coroutines.flow.Flow

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

    val levelOfKnowledgeForCurrentCard: Flow<Int?> = queries
        .getLevelOfKnowledgeForCurrentCard()
        .asFlow()
        .mapToOneOrNull()
}