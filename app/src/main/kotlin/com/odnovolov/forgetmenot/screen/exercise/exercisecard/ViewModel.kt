package com.odnovolov.forgetmenot.screen.exercise.exercisecard

import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.mapToOne
import com.odnovolov.forgetmenot.common.database.mapToOneOrNull
import kotlinx.coroutines.flow.Flow

open class ExerciseCardViewModel(id: Long) {
    protected val queries = database.exerciseCardViewModelQueries

    val question: Flow<String?> = queries.getQuestion(id).asFlow().mapToOneOrNull()
    val answer: Flow<String?> = queries.getAnswer(id).asFlow().mapToOneOrNull()
    val isLearned: Flow<Boolean?> = queries.isLearned(id).asFlow().mapToOneOrNull()
    val isQuestionDisplayed: Flow<Boolean> = queries.isQuestionDisplayed(id).asFlow().mapToOne()
}