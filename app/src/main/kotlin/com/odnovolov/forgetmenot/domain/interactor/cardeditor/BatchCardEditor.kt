package com.odnovolov.forgetmenot.domain.interactor.cardeditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableList
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck

class BatchCardEditor(
    val state: State
) {
    class State(
        editableCards: Collection<EditableCard> = emptyList()
    ) : FlowMaker<State>() {
        var selectedCards: Collection<EditableCard> by flowMaker(editableCards)
    }
    
    private var cancelLastAction: (() -> Unit)? = null

    fun addCardToSelection(editableCard: EditableCard) {
        state.selectedCards =
            state.selectedCards.associateBy { it.card.id }
                .plus(editableCard.card.id to editableCard)
                .values
    }

    fun addCardsToSelection(editableCards: Collection<EditableCard>) {
        val addedAssociatedEditableCards = editableCards.associateBy { it.card.id }
        state.selectedCards =
            state.selectedCards.associateBy { it.card.id }
                .plus(addedAssociatedEditableCards)
                .values
    }

    fun removeCardFromSelection(cardId: Long) {
        state.selectedCards = state.selectedCards
            .filter { editableCard: EditableCard -> editableCard.card.id != cardId }
    }
    
    fun remove() {
        val removingData: Map<Deck, List<Long>> = state.selectedCards.groupBy(
            keySelector = { editableCard: EditableCard -> editableCard.deck },
            valueTransform = { editableCard: EditableCard -> editableCard.card.id }
        )
        val backup: Map<Deck, CopyableList<Card>> =
            removingData.mapValues { (deck: Deck, _) -> deck.cards }
        cancelLastAction = {
            backup.forEach { (deck: Deck, backupCards: CopyableList<Card>) ->
                deck.cards = backupCards
            }
        }
        removingData.forEach { (deck: Deck, removingCardIds: List<Long>) ->
            deck.cards = deck.cards
                .filter { card: Card -> card.id !in removingCardIds }
                .toCopyableList()
        }
        clearSelection()
    }
    
    fun cancelLastAction() {
        cancelLastAction?.invoke()
        cancelLastAction = null
    }

    fun clearSelection() {
        state.selectedCards = emptyList()
    }
}