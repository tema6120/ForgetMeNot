package com.odnovolov.forgetmenot.presentation.screen.deckcontent

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.State
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForEditingExistingDeck
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
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
                navigateToCardsEditor(event.cardId)
            }

            AddCardButtonClicked -> {
                navigateToCardsEditor()
            }
        }
    }

    private fun navigateToCardsEditor(cardId: Long? = null) {
        navigator.navigateToCardsEditorFromDeckSetup {
            val deck = deckEditor.state.deck
            val editableCards: List<EditableCard> =
                deck.cards.map { card -> EditableCard(card) } + EditableCard()
            val currentPosition: Int =
                if (cardId == null) editableCards.lastIndex
                else deck.cards.indexOfFirst { card -> card.id == cardId }
            val cardsEditorState = State(editableCards, currentPosition)
            val cardsEditor = CardsEditorForEditingExistingDeck(deck, cardsEditorState)
            CardsEditorDiScope.create(cardsEditor)
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}