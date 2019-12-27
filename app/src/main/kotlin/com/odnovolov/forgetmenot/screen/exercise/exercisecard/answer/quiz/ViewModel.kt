package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.quiz

import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.mapToOne
import com.odnovolov.forgetmenot.common.database.mapToOneOrNull
import com.odnovolov.forgetmenot.screen.exercise.exercisecards.ExerciseCardViewModelQueries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AnswerQuizTestViewModel(id: Long) {
    private val queries: ExerciseCardViewModelQueries = database.exerciseCardViewModelQueries

    val variant1: Flow<String?> = queries.getVariantText(id, 1).asFlow().mapToOneOrNull()
    val variant2: Flow<String?> = queries.getVariantText(id, 2).asFlow().mapToOneOrNull()
    val variant3: Flow<String?> = queries.getVariantText(id, 3).asFlow().mapToOneOrNull()
    val variant4: Flow<String?> = queries.getVariantText(id, 4).asFlow().mapToOneOrNull()

    val variant1Status: Flow<VariantStatus> = queries
        .getVariantStatus(1, id)
        .asFlow()
        .mapToOne()
        .map { VariantStatus.valueOf(it) }

    val variant2Status: Flow<VariantStatus> = queries
        .getVariantStatus(2, id)
        .asFlow()
        .mapToOne()
        .map { VariantStatus.valueOf(it) }

    val variant3Status: Flow<VariantStatus> = queries
        .getVariantStatus(3, id)
        .asFlow()
        .mapToOne()
        .map { VariantStatus.valueOf(it) }

    val variant4Status: Flow<VariantStatus> = queries
        .getVariantStatus(4, id)
        .asFlow()
        .mapToOne()
        .map { VariantStatus.valueOf(it) }

    val isAnswered: Flow<Boolean> = queries.isAnswered(id).asFlow().mapToOne()

    val isLearned: Flow<Boolean> = queries.isLearned(id).asFlow().mapToOne()
}