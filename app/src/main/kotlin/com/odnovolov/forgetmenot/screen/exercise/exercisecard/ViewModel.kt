package com.odnovolov.forgetmenot.screen.exercise.exercisecard

import com.odnovolov.forgetmenot.common.database.*
import com.odnovolov.forgetmenot.common.entity.TestMethod
import kotlinx.coroutines.flow.Flow

class ExerciseCardViewModel(id: Long) {
    private val queries = database.exerciseCardViewModelQueries

    val testMethod: TestMethod? = queries
        .getTestMethod(id)
        .executeAsOneOrNull()
        ?.let(testMethodAdapter::decode)

    val question: Flow<String> = queries
        .getQuestion(id)
        .asFlow()
        .mapToOneNotNull()

    val isQuestionDisplayed: Flow<Boolean> = queries
        .isQuestionDisplayed(id)
        .asFlow()
        .mapToOneNotNull()

    val isLearned: Flow<Boolean> = queries
        .isLearned(id)
        .asFlow()
        .mapToOneNotNull()
}