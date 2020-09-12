package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting

class DeckReviewPreference(
    deckSorting: DeckSorting = DeckSorting.Default,
    displayOnlyWithTasks: Boolean = false
) : FlowMakerWithRegistry<DeckReviewPreference>() {
    var deckSorting: DeckSorting by flowMaker(deckSorting)
    var displayOnlyWithTasks: Boolean by flowMaker(displayOnlyWithTasks)

    override fun copy() = DeckReviewPreference(deckSorting, displayOnlyWithTasks)
}