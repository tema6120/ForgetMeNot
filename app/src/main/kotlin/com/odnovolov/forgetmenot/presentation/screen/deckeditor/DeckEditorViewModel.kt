package com.odnovolov.forgetmenot.presentation.screen.deckeditor

import com.odnovolov.forgetmenot.domain.entity.Deck
import kotlinx.coroutines.flow.Flow

class DeckEditorViewModel(
    private val screenState: DeckEditorScreenState
) {
    val tabs: DeckEditorTabs get() = screenState.tabs
    val deckName: Flow<String> = screenState.deck.flowOf(Deck::name)
}