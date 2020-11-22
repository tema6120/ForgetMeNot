package com.odnovolov.forgetmenot.persistence.longterm.deckreviewpreference

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting

class DeckReviewPreferencePropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.deckReviewPreferenceQueries

    override fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        when (change.property) {
            DeckReviewPreference::deckSorting -> {
                val deckSorting = change.newValue as DeckSorting
                queries.updateDeckSorting(deckSorting)
            }
            DeckReviewPreference::displayOnlyDecksAvailableForExercise -> {
                val displayOnlyWithTasks = change.newValue as Boolean
                queries.updateDisplayOnlyWithTasks(displayOnlyWithTasks)
            }
        }
    }
}