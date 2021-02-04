package com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenState
import kotlinx.coroutines.flow.Flow

class DeckContentViewModel(
    deckEditorScreenState: DeckEditorScreenState
) {
    val cards: Flow<List<Card>> = deckEditorScreenState.deck.flowOf(Deck::cards)
}