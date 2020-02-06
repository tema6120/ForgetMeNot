package com.odnovolov.forgetmenot.persistence.deckreviewpreference

import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference

object DeckReviewPreferencePropertyChangeHandler {
    private val queries = database.deckReviewPreferenceQueries

    fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        when (change.property) {
            DeckReviewPreference::displayOnlyWithTasks -> {
                val displayOnlyWithTasks = change.newValue as Boolean
                queries.updateDisplayOnlyWithTasks(displayOnlyWithTasks)
            }
        }
    }
}