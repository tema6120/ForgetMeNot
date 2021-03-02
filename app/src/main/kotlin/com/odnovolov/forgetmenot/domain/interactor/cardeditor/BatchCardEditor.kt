package com.odnovolov.forgetmenot.domain.interactor.cardeditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker

class BatchCardEditor(
    val state: State
) {
    class State(
        editableCards: Collection<EditableCard> = emptyList()
    ) : FlowMaker<State>() {
        var editableCards: Collection<EditableCard> by flowMaker(editableCards)
    }

    fun addEditableCard(editableCard: EditableCard) {
        state.editableCards =
            state.editableCards.associateBy { it.card.id }
                .plus(editableCard.card.id to editableCard)
                .values
    }

    fun addEditableCards(editableCards: Collection<EditableCard>) {
        val addedAssociatedEditableCards = editableCards.associateBy { it.card.id }
        state.editableCards =
            state.editableCards.associateBy { it.card.id }
                .plus(addedAssociatedEditableCards)
                .values
    }

    fun removeEditableCard(cardId: Long) {
        state.editableCards = state.editableCards
            .filter { editableCard: EditableCard -> editableCard.card.id != cardId }
    }

    fun clearEditableCards() {
        state.editableCards = emptyList()
    }
}