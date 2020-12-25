package com.odnovolov.forgetmenot.presentation.screen.deckeditor

import com.odnovolov.forgetmenot.domain.interactor.deckeditor.DeckEditor
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.catchAndLogException
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorController.Command
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorController.Command.ShowRenameDialogWithText
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorEvent.*

class DeckEditorController(
    private val deckEditor: DeckEditor,
    private val screenState: DeckEditorScreenState,
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
                catchAndLogException { deckEditor.renameDeck(newName) }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        deckEditorStateProvider.save(deckEditor.state)
        screenStateProvider.save(screenState)
    }
}