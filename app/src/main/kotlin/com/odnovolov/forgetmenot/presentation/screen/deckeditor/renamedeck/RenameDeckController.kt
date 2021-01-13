package com.odnovolov.forgetmenot.presentation.screen.deckeditor.renamedeck

import com.odnovolov.forgetmenot.domain.interactor.deckeditor.DeckEditor
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.renamedeck.RenameDeckEvent.OkButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.renamedeck.RenameDeckEvent.TextChanged

class RenameDeckController(
    private val deckEditor: DeckEditor,
    private val dialogState: RenameDeckDialogState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val deckEditorStateProvider: ShortTermStateProvider<DeckEditor.State>,
    private val dialogStateProvider: ShortTermStateProvider<RenameDeckDialogState>
) : BaseController<RenameDeckEvent, Nothing>() {
    override fun handle(event: RenameDeckEvent) {
        when (event) {
            is TextChanged -> {
                dialogState.typedDeckName = event.text
            }

            OkButtonClicked -> {
                val newName = dialogState.typedDeckName
                val success: Boolean = deckEditor.renameDeck(newName)
                if (success) {
                    navigator.navigateUp()
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        deckEditorStateProvider.save(deckEditor.state)
        dialogStateProvider.save(dialogState)
    }
}