package com.odnovolov.forgetmenot.presentation.screen.decklistseditor

import com.odnovolov.forgetmenot.domain.entity.DeckList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class DeckListViewModel(
    initialDeckList: DeckList
) {
    private val deckListFlow = MutableStateFlow(initialDeckList)

    fun setDeckList(deckList: DeckList) {
        deckListFlow.value = deckList
    }

    val deckListId: Flow<Long> = deckListFlow.map { deckList: DeckList -> deckList.id }

    val deckListColor: Flow<Int> = deckListFlow.flatMapLatest { deckList: DeckList ->
        deckList.flowOf(DeckList::color)
    }

    val deckListName: Flow<String> = deckListFlow.map { deckList: DeckList -> deckList.name }

    val deckListSize: Flow<Int> = deckListFlow.map { deckList: DeckList -> deckList.deckIds.size }
}