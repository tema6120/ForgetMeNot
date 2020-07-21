package com.odnovolov.forgetmenot.presentation.screen.deckcontent

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.State.Mode.EditingExistingDeck
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.DeckEditor
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckcontent.DeckContentEvent.*
import com.odnovolov.forgetmenot.presentation.screen.search.SearchDiScope
import com.odnovolov.forgetmenot.presentation.screen.search.SearchScreenState

class DeckContentController(
    private val deckEditor: DeckEditor,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<DeckContentEvent, Nothing>() {
    override fun handle(event: DeckContentEvent) {
        when (event) {
            SearchButtonClicked -> {
                navigator.navigateToSearchFromDeckSetup {
                    SearchDiScope.create(SearchScreenState())
                }
            }

            is CardClicked -> {
                navigator.navigateToCardsEditorFromDeckSetup {
                    val cardsEditorState = CardsEditor.State(
                        mode = EditingExistingDeck(deckEditor.state.deck),
                        currentPosition = deckEditor.state.deck.cards
                            .indexOfFirst { card -> card.id == event.cardId }
                    )
                    CardsEditorDiScope.create(cardsEditorState)
                }
            }

            AddCardButtonClicked -> {
                navigator.navigateToCardsEditorFromDeckSetup {
                    val cardsEditorState = CardsEditor.State(
                        mode = EditingExistingDeck(deckEditor.state.deck),
                        currentPosition = deckEditor.state.deck.cards.lastIndex + 1
                    )
                    CardsEditorDiScope.create(cardsEditorState)
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}