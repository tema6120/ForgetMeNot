package com.odnovolov.forgetmenot.screen.exercise

import com.odnovolov.forgetmenot.common.database.*
import kotlinx.coroutines.flow.Flow

class ExerciseViewModel {
    private val queries: ExerciseViewModelQueries = database.exerciseViewModelQueries

    val exerciseCardsIdsAtStart: List<Long>
        get() = queries
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

    val isHintButtonVisible: Flow<Boolean> = queries
        .isHintButtonVisible()
        .asFlow()
        .mapToOneNotNull()

    val levelOfKnowledgeForCurrentCard: Flow<Int?> = queries
        .getLevelOfKnowledgeForCurrentCard()
        .asFlow()
        .mapToOneOrNull()

    val isWalkingMode: Boolean = queries
        .isWalkingMode()
        .executeAsOne()
}