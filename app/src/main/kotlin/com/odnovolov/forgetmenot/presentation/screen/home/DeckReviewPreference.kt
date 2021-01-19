package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry

class DeckReviewPreference(
    deckSorting: DeckSorting,
    displayOnlyDecksAvailableForExercise: Boolean
) : FlowMakerWithRegistry<DeckReviewPreference>() {
    var deckSorting: DeckSorting by flowMaker(deckSorting)
    var displayOnlyDecksAvailableForExercise: Boolean by flowMaker(displayOnlyDecksAvailableForExercise)

    override fun copy() = DeckReviewPreference(deckSorting, displayOnlyDecksAvailableForExercise)

    companion object {
        const val DEFAULT_DISPLAY_ONLY_DECKS_AVAILABLE_FOR_EXERCISE = false
    }
}