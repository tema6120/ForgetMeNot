package com.odnovolov.forgetmenot.presentation.screen.decklistseditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.interactor.decklistseditor.DeckListsEditor
import com.odnovolov.forgetmenot.domain.interactor.decklistseditor.EditableDeckList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class DeckListsEditorViewModel(
    deckListsEditorState: DeckListsEditor.State,
    private val screenState: DeckListEditorScreenState
) {
    val deckLists: Flow<List<EditableDeckList>> = deckListsEditorState
        .flowOf(DeckListsEditor.State::editingDeckLists)
        .map { editingDeckLists: List<EditableDeckList> -> editingDeckLists.drop(1) }
        .share()

    val newDeckListId: Flow<Long> = deckListsEditorState
        .flowOf(DeckListsEditor.State::editingDeckLists)
        .map { deckLists: List<EditableDeckList> -> deckLists[0].deckList.id }

    val newDeckListColor: Flow<Int> = deckListsEditorState
        .flowOf(DeckListsEditor.State::editingDeckLists)
        .flatMapLatest { deckLists: List<EditableDeckList> ->
            deckLists[0].flowOf(EditableDeckList::color)
        }

    val isForCreation: Boolean
        get() = screenState.isForCreation
}