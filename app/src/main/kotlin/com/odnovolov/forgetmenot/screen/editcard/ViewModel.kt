package com.odnovolov.forgetmenot.screen.editcard

import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.mapToOne
import kotlinx.coroutines.flow.Flow

class EditCardViewModel {
    private val queries: EditCardViewModelQueries = database.editCardViewModelQueries

    val question: String
        get() = queries.getQuestion().executeAsOne()

    val answer: String
        get() = queries.getAnswer().executeAsOne()
    
    val isDoneButtonEnabled: Flow<Boolean> = queries
        .isQuestionAndAnswerNotEmpty()
        .asFlow()
        .mapToOne()
}