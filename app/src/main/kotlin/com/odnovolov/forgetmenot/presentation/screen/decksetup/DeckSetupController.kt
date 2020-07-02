package com.odnovolov.forgetmenot.presentation.screen.decksetup

import com.odnovolov.forgetmenot.domain.interactor.deckeditor.DeckEditor
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.catchAndLogException
import com.odnovolov.forgetmenot.presentation.screen.decksetup.DeckSetupController.Command
import com.odnovolov.forgetmenot.presentation.screen.decksetup.DeckSetupController.Command.ShowRenameDialogWithText
import com.odnovolov.forgetmenot.presentation.screen.decksetup.DeckSetupEvent.*

class DeckSetupController(
    private val deckEditor: DeckEditor,
    private val screenState: DeckSetupScreenState,
    private val longTermStateSaver: LongTermStateSaver,
    private val deckEditorStateProvider: ShortTermStateProvider<DeckEditor.State>,
    private val screenStateProvider: ShortTermStateProvider<DeckSetupScreenState>
) : BaseController<DeckSetupEvent, Command>() {
    sealed class Command {
        data class ShowRenameDialogWithText(val text: String) : Command()
    }

    override fun handle(event: DeckSetupEvent) {
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