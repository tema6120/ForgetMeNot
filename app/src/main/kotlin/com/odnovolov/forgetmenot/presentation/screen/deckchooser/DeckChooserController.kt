package com.odnovolov.forgetmenot.presentation.screen.deckchooser

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserEvent.*
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserScreenState.Purpose.ChooseDeckWhereToImportCards
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportDiScope
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportEvent.TargetDeckIsSelected
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Direction.Asc
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Direction.Desc

class DeckChooserController(
    private val deckReviewPreference: DeckReviewPreference,
    private val screenState: DeckChooserScreenState,
    private val globalState: GlobalState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val screenStateProvider: ShortTermStateProvider<DeckChooserScreenState>
) : BaseController<DeckChooserEvent, Nothing>() {
    override fun handle(event: DeckChooserEvent) {
        when (event) {
            CancelButtonClicked -> {
                navigator.navigateUp()
            }

            is SearchTextChanged -> {
                screenState.searchText = event.searchText
            }

            SortingDirectionButtonClicked -> {
                with(deckReviewPreference) {
                    val newDirection = if (deckSorting.direction == Asc) Desc else Asc
                    deckSorting = deckSorting.copy(direction = newDirection)
                }
            }

            is SortByButtonClicked -> {
                with(deckReviewPreference) {
                    deckSorting = if (event.criterion == deckSorting.criterion) {
                        val newDirection = if (deckSorting.direction == Asc) Desc else Asc
                        deckSorting.copy(direction = newDirection)
                    } else {
                        deckSorting.copy(criterion = event.criterion)
                    }
                }
            }

            is DeckButtonClicked -> {
                val deck: Deck = globalState.decks.first { it.id == event.deckId }
                when (screenState.purpose) {
                    ChooseDeckWhereToImportCards -> {
                        FileImportDiScope.getOrRecreate().controller
                            .dispatch(TargetDeckIsSelected(deck))
                    }
                }
                navigator.navigateUp()
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        screenStateProvider.save(screenState)
    }
}