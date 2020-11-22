package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry

class DeckReviewPreference(
    deckSorting: DeckSorting = DeckSorting.Default,
    displayOnlyDecksAvailableForExercise: Boolean = false
) : FlowMakerWithRegistry<DeckReviewPreference>() {
    var deckSorting: DeckSorting by flowMaker(deckSorting)
    var displayOnlyDecksAvailableForExercise: Boolean by flowMaker(displayOnlyDecksAvailableForExercise)

    override fun copy() = DeckReviewPreference(deckSorting, displayOnlyDecksAvailableForExercise)
}