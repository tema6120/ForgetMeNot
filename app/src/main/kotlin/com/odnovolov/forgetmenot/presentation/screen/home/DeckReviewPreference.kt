package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.architecturecomponents.RegistrableFlowableState
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting

class DeckReviewPreference(
    deckSorting: DeckSorting = DeckSorting.Default,
    displayOnlyWithTasks: Boolean = false
) : RegistrableFlowableState<DeckReviewPreference>() {
    var deckSorting: DeckSorting by me(deckSorting)
    var displayOnlyWithTasks: Boolean by me(displayOnlyWithTasks)

    override fun copy() = DeckReviewPreference(deckSorting, displayOnlyWithTasks)
}