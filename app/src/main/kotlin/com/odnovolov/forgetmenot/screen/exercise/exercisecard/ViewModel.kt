package com.odnovolov.forgetmenot.screen.exercise.exercisecard

import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.mapToOne
import com.odnovolov.forgetmenot.common.database.mapToOneOrNull
import com.odnovolov.forgetmenot.common.entity.TestMethod
import kotlinx.coroutines.flow.Flow

class ExerciseCardViewModel(id: Long) {
    private val queries = database.exerciseCardViewModelQueries

    val testMethod: TestMethod = queries.getTestMethod(id).executeAsOne()
    val question: Flow<String?> = queries.getQuestion(id).asFlow().mapToOneOrNull()
    val isQuestionDisplayed: Flow<Boolean> = queries.isQuestionDisplayed(id).asFlow().mapToOne()
    val isLearned: Flow<Boolean?> = queries.isLearned(id).asFlow().mapToOneOrNull()
}