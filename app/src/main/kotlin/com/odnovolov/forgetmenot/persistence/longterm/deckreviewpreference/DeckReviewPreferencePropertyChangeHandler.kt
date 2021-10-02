package com.odnovolov.forgetmenot.persistence.longterm.deckreviewpreference

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.entity.DeckList
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting

class DeckReviewPreferencePropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.deckReviewPreferenceQueries

    override fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        val deckReviewPreferenceId = change.propertyOwnerId
        when (change.property) {
            DeckReviewPreference::deckList -> {
                val deckList = change.newValue as DeckList?
                queries.updateDeckListId(deckList?.id, deckReviewPreferenceId)
            }
            DeckReviewPreference::deckSorting -> {
                val deckSorting = change.newValue as DeckSorting
                queries.updateDeckSorting(
                    deckSorting.criterion,
                    deckSorting.direction,
                    deckSorting.newDecksFirst,
                    deckReviewPreferenceId
                )
            }
            DeckReviewPreference::displayOnlyDecksAvailableForExercise -> {
                val displayOnlyDecksAvailableForExercise = change.newValue as Boolean
                queries.updateDisplayOnlyDecksAvailableForExercise(
                    displayOnlyDecksAvailableForExercise,
                    deckReviewPreferenceId
                )
            }
        }
    }
}