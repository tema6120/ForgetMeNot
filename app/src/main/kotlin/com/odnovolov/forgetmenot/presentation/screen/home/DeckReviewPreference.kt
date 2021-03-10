package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import com.odnovolov.forgetmenot.domain.entity.DeckList

class DeckReviewPreference(
    currentDeckList: DeckList?,
    deckSorting: DeckSorting,
    displayOnlyDecksAvailableForExercise: Boolean
) : FlowMakerWithRegistry<DeckReviewPreference>() {
    var currentDeckList: DeckList? by flowMakerForCopyable(currentDeckList)
    var deckSorting: DeckSorting by flowMaker(deckSorting)
    var displayOnlyDecksAvailableForExercise: Boolean by flowMaker(displayOnlyDecksAvailableForExercise)

    override fun copy() = DeckReviewPreference(
        currentDeckList,
        deckSorting,
        displayOnlyDecksAvailableForExercise
    )

    companion object {
        const val DEFAULT_DISPLAY_ONLY_DECKS_AVAILABLE_FOR_EXERCISE = false
        const val DEFAULT_DECK_LIST_COLOR = 0xFF474747.toInt()
    }
}