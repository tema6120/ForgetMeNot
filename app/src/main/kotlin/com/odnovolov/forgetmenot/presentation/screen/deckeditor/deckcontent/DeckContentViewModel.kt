package com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableList
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.BatchCardEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenState
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent.ItemInDeckContentList.SelectableCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class DeckContentViewModel(
    deckEditorScreenState: DeckEditorScreenState,
    batchCardEditorState: BatchCardEditor.State
) {
    val cards: Flow<List<ItemInDeckContentList>> = combine(
        deckEditorScreenState.deck.flowOf(Deck::cards),
        batchCardEditorState.flowOf(BatchCardEditor.State::selectedCards)
    ) { cards: CopyableList<Card>, editableCards: Collection<EditableCard> ->
        val result = ArrayList<ItemInDeckContentList>(cards.size + 1)
        result.add(ItemInDeckContentList.Header)
        val selectedCardIds: List<Long> =
            editableCards.map { editableCard: EditableCard -> editableCard.card.id }
        for (card: Card in cards) {
            val isSelected = card.id in selectedCardIds
            val selectableCard = SelectableCard(card.copy(), isSelected)
            result.add(selectableCard)
        }
        result
    }
}