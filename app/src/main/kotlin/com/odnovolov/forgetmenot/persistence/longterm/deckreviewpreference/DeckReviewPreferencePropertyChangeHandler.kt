package com.odnovolov.forgetmenot.persistence.longterm.deckreviewpreference

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.persistence.DbKeys
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting

class DeckReviewPreferencePropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.keyValueQueries

    override fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        when (change.property) {
            DeckReviewPreference::deckSorting -> {
                val deckSorting = change.newValue as DeckSorting
                queries.replace(
                    key = DbKeys.DECK_SORTING_CRITERION,
                    value = deckSorting.criterion.name
                )
                queries.replace(
                    key = DbKeys.DECK_SORTING_DIRECTION,
                    value = deckSorting.direction.name
                )
            }
            DeckReviewPreference::displayOnlyDecksAvailableForExercise -> {
                val displayOnlyWithTasks = change.newValue as Boolean
                queries.replace(
                    key = DbKeys.DISPLAY_ONLY_DECKS_AVAILABLE_FOR_EXERCISE,
                    value = displayOnlyWithTasks.toString()
                )
            }
        }
    }
}