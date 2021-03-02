package com.odnovolov.forgetmenot.presentation.screen.deckeditor

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.BatchCardEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.State
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForEditingDeck
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorEvent.*
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDiScope
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDialogPurpose.ToRenameExistingDeck
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDialogState

class DeckEditorController(
    private val batchCardEditor: BatchCardEditor,
    private val screenState: DeckEditorScreenState,
    private val navigator: Navigator,
    private val globalState: GlobalState
) : BaseController<DeckEditorEvent, Nothing>() {
    override val autoSave = false

    override fun handle(event: DeckEditorEvent) {
        when (event) {
            RenameDeckButtonClicked -> {
                navigator.showRenameDeckDialogFromDeckEditor {
                    val deck = screenState.deck
                    val dialogState = RenameDeckDialogState(
                        purpose = ToRenameExistingDeck(deck),
                        typedDeckName = deck.name
                    )
                    RenameDeckDiScope.create(dialogState)
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
                    val cardsEditor = CardsEditorForEditingDeck(
                        deck,
                        isNewDeck = false,
                        cardsEditorState,
                        globalState
                    )
                    CardsEditorDiScope.create(cardsEditor)
                }
            }

            CancelledCardSelection -> {
                batchCardEditor.clearEditableCards()
            }

            SelectAllCardsButtonClicked -> {
                val deck: Deck = screenState.deck
                val allEditableCards: List<EditableCard> =
                    deck.cards.map { card: Card -> EditableCard(card, deck) }
                batchCardEditor.addEditableCards(allEditableCards)
            }

            RemoveCardsOptionSelected -> {
            }
        }
    }

    override fun saveState() {}
}