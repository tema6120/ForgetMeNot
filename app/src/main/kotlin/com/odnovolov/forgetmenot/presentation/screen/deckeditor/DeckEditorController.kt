package com.odnovolov.forgetmenot.presentation.screen.deckeditor

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.State
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForEditingExistingDeck
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.DeckEditor
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorEvent.AddCardButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorEvent.RenameDeckButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.renamedeck.RenameDeckDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.renamedeck.RenameDeckDialogState

class DeckEditorController(
    private val screenState: DeckEditorScreenState,
    private val navigator: Navigator
) : BaseController<DeckEditorEvent, Nothing>() {
    override val autoSave = false

    override fun handle(event: DeckEditorEvent) {
        when (event) {
            RenameDeckButtonClicked -> {
                navigator.showRenameDeckDialogFromDeckEditor {
                    val deck = screenState.deck
                    val deckEditorState = DeckEditor.State(deck)
                    val dialogState = RenameDeckDialogState(deck.name)
                    RenameDeckDiScope.create(deckEditorState, dialogState)
                }
            }

            AddCardButtonClicked -> {
                navigator.navigateToCardsEditorFromDeckEditor {
                    val deck = screenState.deck
                    val editableCards: List<EditableCard> =
                        deck.cards.map { card -> EditableCard(card, deck) }
                            .plus(EditableCard(Card(generateId(), "", ""), deck))
                    val position: Int = editableCards.lastIndex
                    val cardsEditorState = State(editableCards, position)
                    val cardsEditor = CardsEditorForEditingExistingDeck(deck, cardsEditorState)
                    CardsEditorDiScope.create(cardsEditor)
                }
            }
        }
    }

    override fun saveState() {}
}