package com.odnovolov.forgetmenot.presentation.screen.deckcontent

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.DeckEditor
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckcontent.DeckContentController.Command
import com.odnovolov.forgetmenot.presentation.screen.deckcontent.DeckContentEvent.AddCardButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.deckcontent.DeckContentEvent.CardClicked

class DeckContentController(
    private val deckEditor: DeckEditor,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<DeckContentEvent, Command>() {
    sealed class Command {
    }

    override fun handle(event: DeckContentEvent) {
        when (event) {
            is CardClicked -> {
                navigator.navigateToCardsEditor {
                    val cardsEditorState = CardsEditor.State(
                        deck = deckEditor.state.deck,
                        currentPosition = deckEditor.state.deck.cards
                            .indexOfFirst { card -> card.id == event.cardId }
                    )
                    CardsEditorDiScope.create(cardsEditorState)
                }
            }

            AddCardButtonClicked -> {
                navigator.navigateToCardsEditor {
                    val cardsEditorState = CardsEditor.State(
                        deck = deckEditor.state.deck,
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