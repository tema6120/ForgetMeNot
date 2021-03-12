package com.odnovolov.forgetmenot.presentation.screen.decklistseditor

import com.odnovolov.forgetmenot.domain.entity.DeckList
import com.odnovolov.forgetmenot.domain.interactor.decklistseditor.DeckListsEditor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DeckListsEditorViewModel(
    deckListsEditorState: DeckListsEditor.State,
    private val screenState: DeckListEditorScreenState
) {
    val deckLists: Flow<List<DeckList>> = deckListsEditorState
        .flowOf(DeckListsEditor.State::editingDeckLists)
        .map { editingDeckLists: List<DeckList> -> editingDeckLists.drop(1) }

    val isForCreation: Boolean
        get() = screenState.isForCreation
}