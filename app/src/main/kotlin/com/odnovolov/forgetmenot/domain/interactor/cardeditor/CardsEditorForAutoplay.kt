package com.odnovolov.forgetmenot.domain.interactor.cardeditor

import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.SavingResult.Success

class CardsEditorForAutoplay(
    private val player: Player,
    removedCards: MutableList<EditableCard> = ArrayList(),
    state: State
) : CardsEditorForEditingSpecificCards(
    removedCards,
    state
) {
    override fun save(): SavingResult {
        return super.save().also { result ->
            if (result is Success) {
                val removedCardIds: List<Long> =
                    removedCards.map { editableCard: EditableCard -> editableCard.card.id }
                player.notifyCardsRemoved(removedCardIds)
            }
        }
    }
}