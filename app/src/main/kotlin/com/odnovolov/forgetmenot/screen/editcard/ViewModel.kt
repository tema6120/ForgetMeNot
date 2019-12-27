package com.odnovolov.forgetmenot.screen.editcard

import com.odnovolov.forgetmenot.common.database.database

class EditCardViewModel {
    private val queries: EditCardViewModelQueries = database.editCardViewModelQueries

    val question: String = queries.getQuestion().executeAsOne()
    val answer: String = queries.getAnswer().executeAsOne()
}