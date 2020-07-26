package com.odnovolov.forgetmenot.presentation.screen.home.decksorting

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting.Direction.Asc
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting.Direction.Desc
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSortingCommand.DismissBottomSheet
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSortingEvent.SortByButtonClicked

class DeckSortingController(
    private val deckReviewPreference: DeckReviewPreference,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<DeckSortingEvent, DeckSortingCommand>() {
    override fun handle(event: DeckSortingEvent) {
        when (event) {
            is SortByButtonClicked -> {
                with(deckReviewPreference) {
                    deckSorting = if (event.criterion == deckSorting.criterion) {
                        val newDirection = if (deckSorting.direction == Asc) Desc else Asc
                        deckSorting.copy(direction = newDirection)
                    } else {
                        deckSorting.copy(criterion = event.criterion)
                    }
                }
                sendCommand(DismissBottomSheet)
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}