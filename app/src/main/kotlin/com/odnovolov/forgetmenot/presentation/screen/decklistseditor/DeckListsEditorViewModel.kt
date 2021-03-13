package com.odnovolov.forgetmenot.presentation.screen.decklistseditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.DeckList
import com.odnovolov.forgetmenot.domain.interactor.decklistseditor.DeckListsEditor
import kotlinx.coroutines.flow.*

class DeckListsEditorViewModel(
    deckListsEditorState: DeckListsEditor.State,
    private val screenState: DeckListEditorScreenState
) {
    val deckLists: Flow<List<DeckList>> = deckListsEditorState
        .flowOf(DeckListsEditor.State::editingDeckLists)
        .map { editingDeckLists: List<DeckList> -> editingDeckLists.drop(1) }
        .share()

    val newDeckListId: Flow<Long> = deckListsEditorState
        .flowOf(DeckListsEditor.State::editingDeckLists)
        .map { deckLists: List<DeckList> -> deckLists[0].id }

    val newDeckListColor: Flow<Int> = deckListsEditorState
        .flowOf(DeckListsEditor.State::editingDeckLists)
        .flatMapLatest { deckLists: List<DeckList> -> deckLists[0].flowOf(DeckList::color) }

    val isForCreation: Boolean
        get() = screenState.isForCreation
}