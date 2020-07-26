package com.odnovolov.forgetmenot.presentation.screen.decksetup.deckcontent

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.DeckEditor
import kotlinx.coroutines.flow.Flow

class DeckContentViewModel(
    deckEditorState: DeckEditor.State
) {
    val cards: Flow<List<Card>> = deckEditorState.deck.flowOf(Deck::cards)
}