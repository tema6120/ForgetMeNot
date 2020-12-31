package com.odnovolov.forgetmenot.presentation.screen.deckeditor

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForEditingExistingDeck
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.DeckEditor
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.doWithCatchingExceptions
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorController.Command
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorController.Command.ShowRenameDialogWithText
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorEvent.*

class DeckEditorController(
    private val deckEditor: DeckEditor,
    private val screenState: DeckEditorScreenState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val deckEditorStateProvider: ShortTermStateProvider<DeckEditor.State>,
    private val screenStateProvider: ShortTermStateProvider<DeckEditorScreenState>
) : BaseController<DeckEditorEvent, Command>() {
    sealed class Command {
        data class ShowRenameDialogWithText(val text: String) : Command()
    }

    override fun handle(event: DeckEditorEvent) {
        when (event) {
            RenameDeckButtonClicked -> {
                val deckName = deckEditor.state.deck.name
                sendCommand(ShowRenameDialogWithText(deckName))
            }

            is RenameDeckDialogTextChanged -> {
                screenState.typedDeckName = event.text
            }

            RenameDeckDialogPositiveButtonClicked -> {
                val newName = screenState.typedDeckName
                doWithCatchingExceptions { deckEditor.renameDeck(newName) }
            }

            AddCardButtonClicked -> {
                navigateToCardsEditor()
            }
        }
    }

    private fun navigateToCardsEditor() {
        navigator.navigateToCardsEditorFromDeckEditor {
            val deck = deckEditor.state.deck
            val editableCards: List<EditableCard> =
                deck.cards.map { card -> EditableCard(card, deck) }
                    .plus(EditableCard(Card(generateId(), "", ""), deck))
            val position: Int = editableCards.lastIndex
            val cardsEditorState = CardsEditor.State(editableCards, position)
            val cardsEditor = CardsEditorForEditingExistingDeck(deck, cardsEditorState)
            CardsEditorDiScope.create(cardsEditor)
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        deckEditorStateProvider.save(deckEditor.state)
        screenStateProvider.save(screenState)
    }
}