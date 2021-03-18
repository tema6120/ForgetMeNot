package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import com.odnovolov.forgetmenot.domain.entity.DeckList

class DeckReviewPreference(
    override val id: Long,
    currentDeckList: DeckList?,
    deckSorting: DeckSorting,
    displayOnlyDecksAvailableForExercise: Boolean
) : FlowMakerWithRegistry<DeckReviewPreference>() {
    var currentDeckList: DeckList? by flowMakerForCopyable(currentDeckList)
    var deckSorting: DeckSorting by flowMaker(deckSorting)
    var displayOnlyDecksAvailableForExercise: Boolean by flowMaker(displayOnlyDecksAvailableForExercise)

    override fun copy() = DeckReviewPreference(
        id,
        currentDeckList,
        deckSorting,
        displayOnlyDecksAvailableForExercise
    )

    companion object {
        const val DEFAULT_DISPLAY_ONLY_DECKS_AVAILABLE_FOR_EXERCISE = false
        const val DEFAULT_DECK_LIST_COLOR = 0xFF474747.toInt()

        const val ID_TO_VIEW = 0L
        const val ID_TO_IMPORT_CARDS = 1L
        const val ID_TO_MERGE = 2L
        const val ID_TO_MOVE = 3L
        const val ID_TO_COPY = 4L
    }
}