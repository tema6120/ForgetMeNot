package com.odnovolov.forgetmenot.screen.exercise.exercisecard

import com.odnovolov.forgetmenot.common.database.*
import com.odnovolov.forgetmenot.common.entity.TestMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseCardViewModel(id: Long) {
    private val queries = database.exerciseCardViewModelQueries

    val testMethod: Flow<TestMethod> = queries
        .getTestMethod(id)
        .asFlow()
        .mapToOneNotNull()
        .map { databaseValue: String -> testMethodAdapter.decode(databaseValue) }

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