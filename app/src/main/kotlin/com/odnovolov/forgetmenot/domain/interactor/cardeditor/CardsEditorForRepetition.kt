package com.odnovolov.forgetmenot.domain.interactor.cardeditor

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition

class CardsEditorForRepetition(
    private val repetition: Repetition,
    removedCards: MutableList<EditableCard> = ArrayList(),
    state: State
) : CardsEditorForEditingSpecificCards(
    removedCards,
    state
) {
    override fun isCurrentCardRemovable(): Boolean =
        state.editableCards.isNotEmpty() && currentEditableCard.card.isNotInRepetition()

    private fun Card.isNotInRepetition(): Boolean =
        id !in repetition.state.repetitionCards.map { it.card.id }
}