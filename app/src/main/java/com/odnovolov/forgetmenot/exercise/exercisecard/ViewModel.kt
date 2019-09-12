package com.odnovolov.forgetmenot.exercise.exercisecard

import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.mapToOne
import kotlinx.coroutines.flow.Flow

open class ExerciseCardViewModel(cardId: Long) {
    protected val queries = database.exerciseCardViewModelQueries

    val question: Flow<String> = queries.getQuestion(cardId).asFlow().mapToOne()
    val answer: Flow<String> = queries.getAnswer(cardId).asFlow().mapToOne()
    val isLearned: Flow<Boolean> = queries.isLearned(cardId).asFlow().mapToOne()
}