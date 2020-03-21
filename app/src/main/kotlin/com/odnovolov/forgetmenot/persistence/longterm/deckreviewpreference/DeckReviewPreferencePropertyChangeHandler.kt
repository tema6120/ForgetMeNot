package com.odnovolov.forgetmenot.persistence.longterm.deckreviewpreference

import com.odnovolov.forgetmenot.persistence.database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting

object DeckReviewPreferencePropertyChangeHandler {
    private val queries = database.deckReviewPreferenceQueries

    fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        when (change.property) {
            DeckReviewPreference::deckSorting -> {
                val deckSorting = change.newValue as DeckSorting
                queries.updateDeckSorting(deckSorting)
            }
            DeckReviewPreference::displayOnlyWithTasks -> {
                val displayOnlyWithTasks = change.newValue as Boolean
                queries.updateDisplayOnlyWithTasks(displayOnlyWithTasks)
            }
        }
    }
}