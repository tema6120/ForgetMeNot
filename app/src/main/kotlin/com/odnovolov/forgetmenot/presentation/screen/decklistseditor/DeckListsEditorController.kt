package com.odnovolov.forgetmenot.presentation.screen.decklistseditor

import com.odnovolov.forgetmenot.domain.entity.DeckList
import com.odnovolov.forgetmenot.domain.interactor.decklistseditor.DeckListsEditor
import com.odnovolov.forgetmenot.domain.interactor.decklistseditor.DeckListsEditor.SaveResult.Success
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.decklistseditor.DeckListsEditorController.Command
import com.odnovolov.forgetmenot.presentation.screen.decklistseditor.DeckListsEditorController.Command.ShowColorChooser
import com.odnovolov.forgetmenot.presentation.screen.decklistseditor.DeckListsEditorEvent.*

class DeckListsEditorController(
    private val deckListsEditor: DeckListsEditor,
    private val screenState: DeckListEditorScreenState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<DeckListsEditorEvent, Command>() {
    sealed class Command {
        class ShowColorChooser(
            val deckListId: Long,
            val selectableColors: List<SelectableDeckListColor>
        ) : Command()
    }

    override fun handle(event: DeckListsEditorEvent) {
        when (event) {
            is SelectDeckListColorButtonClicked -> {
                val deckListForColorChooser = deckListsEditor.state.editingDeckLists
                    .find { deckList: DeckList -> deckList.id == event.deckListId }
                    ?: return
                screenState.deckListForColorChooser = deckListForColorChooser
                val selectableColors: List<SelectableDeckListColor> =
                    predefinedDeckListColors.map { predefinedDeckListColor: Int ->
                        SelectableDeckListColor(
                            predefinedDeckListColor,
                            isSelected = predefinedDeckListColor == deckListForColorChooser.color
                        )
                    }
                sendCommand(ShowColorChooser(deckListForColorChooser.id, selectableColors))
            }

            is ColorIsSelected -> {
                screenState.deckListForColorChooser?.color = event.color
            }

            is NewDeckListNameChanged -> {
                val newDeckListId: Long = deckListsEditor.state.editingDeckLists.first().id
                deckListsEditor.rename(newDeckListId, event.name)
            }

            SaveNewDeckListButtonClicked -> {
                deckListsEditor.createNewDeckList()
            }

            is DeckListNameChanged -> {
                deckListsEditor.rename(event.deckListId, event.name)
            }

            is RemoveDeckListButtonClicked -> {

            }

            DoneButtonClicked -> {
                val saveResult: DeckListsEditor.SaveResult = deckListsEditor.save()
                if (saveResult == Success) {
                    navigator.navigateUp()
                } else {
                    // todo
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}