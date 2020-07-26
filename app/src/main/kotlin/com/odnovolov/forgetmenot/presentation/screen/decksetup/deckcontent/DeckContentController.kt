package com.odnovolov.forgetmenot.presentation.screen.decksetup.deckcontent

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.State
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForEditingExistingDeck
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.DeckEditor
import com.odnovolov.forgetmenot.domain.interactor.deckexporter.DeckExporter
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.decksetup.deckcontent.DeckContentController.Command
import com.odnovolov.forgetmenot.presentation.screen.decksetup.deckcontent.DeckContentController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.decksetup.deckcontent.DeckContentEvent.*
import com.odnovolov.forgetmenot.presentation.screen.search.SearchDiScope
import java.io.IOException

class DeckContentController(
    private val deckEditor: DeckEditor,
    private val deckExporter: DeckExporter,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<DeckContentEvent, Command>() {
    sealed class Command {
        class ShowCreateFileDialog(val fileName: String) : Command()
        object ShowDeckIsExportedMessage : Command()
        class ShowExportErrorMessage(val e: IOException) : Command()
    }

    override fun handle(event: DeckContentEvent) {
        when (event) {
            ExportButtonClicked -> {
                val fileName = deckEditor.state.deck.name
                sendCommand(ShowCreateFileDialog(fileName))
            }

            is OutputStreamOpened -> {
                try {
                    deckExporter.export(
                        deck = deckEditor.state.deck,
                        outputStream = event.outputStream
                    )
                    sendCommand(ShowDeckIsExportedMessage)
                } catch (e: IOException) {
                    sendCommand(ShowExportErrorMessage(e))
                }
            }

            SearchButtonClicked -> {
                navigator.navigateToSearchFromDeckSetup {
                    SearchDiScope()
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
                deck.cards.map { card -> EditableCard(card, deck) }
                    .plus(EditableCard(Card(generateId(), "", ""), deck))
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