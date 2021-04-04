package com.odnovolov.forgetmenot.presentation.screen.decklistseditor

import androidx.core.graphics.toColorInt
import com.odnovolov.forgetmenot.domain.interactor.decklistseditor.DeckListsEditor
import com.odnovolov.forgetmenot.domain.interactor.decklistseditor.DeckListsEditor.SaveResult.Failure
import com.odnovolov.forgetmenot.domain.interactor.decklistseditor.DeckListsEditor.SaveResult.Success
import com.odnovolov.forgetmenot.domain.interactor.decklistseditor.EditableDeckList
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.decklistseditor.DeckListsEditorController.Command
import com.odnovolov.forgetmenot.presentation.screen.decklistseditor.DeckListsEditorController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.decklistseditor.DeckListsEditorEvent.*

class DeckListsEditorController(
    private val deckListsEditor: DeckListsEditor,
    private val screenState: DeckListEditorScreenState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val deckListsEditorStateProvider: ShortTermStateProvider<DeckListsEditor.State>,
    private val screenStateProvider: ShortTermStateProvider<DeckListEditorScreenState>
) : BaseController<DeckListsEditorEvent, Command>() {
    sealed class Command {
        class ShowColorChooserFor(val deckListId: Long) : Command()
        object ShowDeckListIsRemovedMessage : Command()
        class ShowNameCannotBeEmptyMessage(val deckListId: Long) : Command()
        object AskUserToConfirmExit : Command()
    }

    override fun handle(event: DeckListsEditorEvent) {
        when (event) {
            is SelectDeckListColorButtonClicked -> {
                screenState.editableDeckListForColorChooser = deckListsEditor.state.editingDeckLists
                    .find { editableDeckList: EditableDeckList ->
                        editableDeckList.deckList.id == event.deckListId
                    } ?: return
                sendCommand(ShowColorChooserFor(event.deckListId))
            }

            is ColorHexTextWasSelected -> {
                screenState.editableDeckListForColorChooser?.color =
                    try {
                        "#${event.text}".toColorInt()
                    } catch (e: IllegalArgumentException) {
                        return
                    }
            }

            is ColorWasSelected -> {
                screenState.editableDeckListForColorChooser?.color = event.color
            }

            is NewDeckListNameChanged -> {
                val newDeckListId: Long = deckListsEditor.state.editingDeckLists.first().deckList.id
                deckListsEditor.rename(newDeckListId, event.name)
            }

            SaveNewDeckListButtonClicked -> {
                deckListsEditor.createNewDeckList()
            }

            is DeckListNameChanged -> {
                deckListsEditor.rename(event.deckListId, event.name)
            }

            is RemoveDeckListButtonClicked -> {
                val success: Boolean = deckListsEditor.remove(event.deckListId)
                if (success) {
                    sendCommand(ShowDeckListIsRemovedMessage)
                }
            }

            CancelDeckListRemovingButtonClicked -> {
                deckListsEditor.cancelRemoving()
            }

            BackButtonClicked -> {
                if (deckListsEditor.hasChanges()) {
                    sendCommand(AskUserToConfirmExit)
                } else {
                    navigator.navigateUp()
                }
            }

            DoneButtonClicked, SaveButtonClicked -> {
                when (val saveResult: DeckListsEditor.SaveResult = deckListsEditor.save()) {
                    Success -> navigator.navigateUp()
                    is Failure -> sendCommand(ShowNameCannotBeEmptyMessage(saveResult.deckListId))
                }
            }

            UserConfirmedExit -> {
                navigator.navigateUp()
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        deckListsEditorStateProvider.save(deckListsEditor.state)
        screenStateProvider.save(screenState)
    }
}