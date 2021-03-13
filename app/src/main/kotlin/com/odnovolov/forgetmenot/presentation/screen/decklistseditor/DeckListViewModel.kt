package com.odnovolov.forgetmenot.presentation.screen.decklistseditor

import com.odnovolov.forgetmenot.domain.interactor.decklistseditor.EditableDeckList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class DeckListViewModel(
    initialEditableDeckList: EditableDeckList
) {
    private val editableDeckListFlow = MutableStateFlow(initialEditableDeckList)

    fun setDeckList(editableDeckList: EditableDeckList) {
        editableDeckListFlow.value = editableDeckList
    }

    val deckListId: Flow<Long> =
        editableDeckListFlow.map { editableDeckList: EditableDeckList ->
            editableDeckList.deckList.id
        }

    val deckListColor: Flow<Int> =
        editableDeckListFlow.flatMapLatest { editableDeckList: EditableDeckList ->
            editableDeckList.flowOf(EditableDeckList::color)
        }

    val deckListName: Flow<String> =
        editableDeckListFlow.map { editableDeckList: EditableDeckList ->
            editableDeckList.name
        }

    val deckListSize: Flow<Int> =
        editableDeckListFlow.map { editableDeckList: EditableDeckList ->
            editableDeckList.deckList.deckIds.size
        }
}