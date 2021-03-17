package com.odnovolov.forgetmenot.persistence.longterm.cardappearance

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.persistence.DbKeys
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearance
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardTextAlignment

class CardAppearancePropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.keyValueQueries

    override fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        when (change.property) {
            CardAppearance::questionTextAlignment -> {
                val questionTextAlignment = change.newValue as CardTextAlignment
                queries.replace(
                    key = DbKeys.QUESTION_TEXT_ALIGNMENT,
                    value = questionTextAlignment.name
                )
            }
            CardAppearance::questionTextSize -> {
                val questionTextSize = change.newValue as Int
                queries.replace(
                    key = DbKeys.QUESTION_TEXT_SIZE,
                    value = questionTextSize.toString()
                )
            }
            CardAppearance::answerTextAlignment -> {
                val answerTextAlignment = change.newValue as CardTextAlignment
                queries.replace(
                    key = DbKeys.ANSWER_TEXT_ALIGNMENT,
                    value = answerTextAlignment.name
                )
            }
            CardAppearance::answerTextSize -> {
                val answerTextSize = change.newValue as Int
                queries.replace(
                    key = DbKeys.ANSWER_TEXT_SIZE,
                    value = answerTextSize.toString()
                )
            }
        }
    }
}