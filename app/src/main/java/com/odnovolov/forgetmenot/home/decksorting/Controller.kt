package com.odnovolov.forgetmenot.home.decksorting

import com.odnovolov.forgetmenot.common.BaseController
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.home.decksorting.DeckSortingEvent.SortByButtonClicked
import com.odnovolov.forgetmenot.home.decksorting.DeckSortingOrder.DismissBottomSheet

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