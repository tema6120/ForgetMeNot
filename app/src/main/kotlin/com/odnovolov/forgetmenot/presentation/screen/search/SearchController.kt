package com.odnovolov.forgetmenot.presentation.screen.search

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.State.Mode.EditingExistingDeck
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.decksetup.DeckSetupDiScope
import com.odnovolov.forgetmenot.presentation.screen.search.SearchEvent.*
import kotlinx.coroutines.runBlocking

class SearchController(
    private val screenState: SearchScreenState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val searchScreenStateProvider: ShortTermStateProvider<SearchScreenState>
) : BaseController<SearchEvent, Nothing>() {
    override fun handle(event: SearchEvent) {
        when (event) {
            BackButtonClicked -> {
                navigator.navigateUp()
            }

            is SearchTextChanged -> {
                screenState.searchText = event.text
            }

            is CardClicked -> {
                when {
                    DeckSetupDiScope.isOpen() -> {
                        navigator.navigateToCardsEditorFromSearch {
                            val deck = runBlocking {
                                DeckSetupDiScope.get().screenState.relevantDeck
                            }
                            val cardsEditorState = CardsEditor.State(
                                mode = EditingExistingDeck(deck),
                                currentPosition = deck.cards
                                    .indexOfFirst { card -> card.id == event.cardId }
                            )
                            CardsEditorDiScope.create(cardsEditorState)
                        }
                    }
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        searchScreenStateProvider.save(screenState)
    }
}