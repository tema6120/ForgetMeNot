package com.odnovolov.forgetmenot.presentation.screen.home.decksorting

import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting.Direction.Asc
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting.Direction.Desc
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSortingCommand.DismissBottomSheet
import kotlinx.coroutines.flow.Flow

class DeckSortingController(
    private val deckReviewPreference: DeckReviewPreference,
    private val longTermStateSaver: LongTermStateSaver
) {
    private val commandFlow = EventFlow<DeckSortingCommand>()
    val commands: Flow<DeckSortingCommand> = commandFlow.get()

    fun onSortByButtonClicked(criterion: DeckSorting.Criterion) {
        with (deckReviewPreference) {
            deckSorting = if (criterion === deckSorting.criterion) {
                val newDirection = if (deckSorting.direction === Asc) Desc else Asc
                deckSorting.copy(direction = newDirection)
            } else {
                deckSorting.copy(criterion = criterion)
            }
        }
        longTermStateSaver.saveStateByRegistry()
        commandFlow.send(DismissBottomSheet)
    }
}