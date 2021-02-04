package com.odnovolov.forgetmenot.presentation.screen.renamedeck

import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.renameDeck
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckEvent.OkButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckEvent.TextChanged

class RenameDeckController(
    private val dialogState: RenameDeckDialogState,
    private val globalState: GlobalState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val dialogStateProvider: ShortTermStateProvider<RenameDeckDialogState>
) : BaseController<RenameDeckEvent, Nothing>() {
    override fun handle(event: RenameDeckEvent) {
        when (event) {
            is TextChanged -> {
                dialogState.typedDeckName = event.text
            }

            OkButtonClicked -> {
                val success: Boolean = renameDeck(
                    newName = dialogState.typedDeckName,
                    dialogState.abstractDeck,
                    globalState
                )
                if (success) {
                    navigator.navigateUp()
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        dialogStateProvider.save(dialogState)
    }
}