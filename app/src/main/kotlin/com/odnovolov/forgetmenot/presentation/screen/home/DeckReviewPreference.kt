package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import com.odnovolov.forgetmenot.domain.entity.DeckList

class DeckReviewPreference(
    override val id: Long,
    deckList: DeckList?,
    deckSorting: DeckSorting,
    displayOnlyDecksAvailableForExercise: Boolean
) : FlowMakerWithRegistry<DeckReviewPreference>() {
    var deckList: DeckList? by flowMakerForCopyable(deckList)
    var deckSorting: DeckSorting by flowMaker(deckSorting)
    var displayOnlyDecksAvailableForExercise: Boolean by flowMaker(displayOnlyDecksAvailableForExercise)

    override fun copy() = DeckReviewPreference(
        id,
        deckList,
        deckSorting,
        displayOnlyDecksAvailableForExercise
    )

    companion object {
        const val DEFAULT_DECK_LIST_COLOR = 0xFF7F7F7F.toInt()

        const val ID_TO_VIEW = 0L
        const val ID_TO_IMPORT_CARDS = 1L
        const val ID_TO_MERGE = 2L
        const val ID_TO_MOVE = 3L
        const val ID_TO_COPY = 4L
    }
}