package com.odnovolov.forgetmenot.screen.home.decksorting

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.screen.home.decksorting.DeckSorting.Direction.ASC
import com.odnovolov.forgetmenot.screen.home.decksorting.DeckSorting.Direction.DESC
import com.odnovolov.forgetmenot.screen.home.decksorting.DeckSortingEvent.SortByButtonClicked
import com.odnovolov.forgetmenot.screen.home.decksorting.DeckSortingOrder.DismissBottomSheet

class DeckSortingController : BaseController<DeckSortingEvent, DeckSortingOrder>() {
    private val queries: DeckSortingControllerQueries = database.deckSortingControllerQueries

    override fun handleEvent(event: DeckSortingEvent) {
        return when (event) {
            is SortByButtonClicked -> {
                val deckSorting: DeckSorting = queries.getDeckSorting(::DeckSorting).executeAsOne()
                if (deckSorting.criterion === event.criterion) {
                    val newDirection = if (deckSorting.direction === ASC) DESC else ASC
                    queries.setDirection(newDirection)
                } else {
                    queries.setCriterion(event.criterion)
                }
                issueOrder(DismissBottomSheet)
            }
        }
    }

}