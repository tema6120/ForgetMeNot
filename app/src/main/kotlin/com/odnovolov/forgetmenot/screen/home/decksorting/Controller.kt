package com.odnovolov.forgetmenot.screen.home.decksorting

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.screen.home.decksorting.DeckSortingEvent.SortByButtonClicked
import com.odnovolov.forgetmenot.screen.home.decksorting.DeckSortingOrder.DismissBottomSheet

class DeckSortingController : BaseController<DeckSortingEvent, DeckSortingOrder>() {

    override fun handleEvent(event: DeckSortingEvent) {
        return when (event) {
            is SortByButtonClicked -> {
                database.deckSortingControllerQueries.setDeckSorting(event.deckSorting)
                issueOrder(DismissBottomSheet)
            }
        }
    }

}