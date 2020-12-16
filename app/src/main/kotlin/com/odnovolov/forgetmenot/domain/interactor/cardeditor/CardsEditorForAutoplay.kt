package com.odnovolov.forgetmenot.domain.interactor.cardeditor

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player

class CardsEditorForAutoplay(
    private val player: Player,
    removedCards: MutableList<EditableCard> = ArrayList(),
    state: State
) : CardsEditorForEditingSpecificCards(
    removedCards,
    state
) {
    override fun isCurrentCardRemovable(): Boolean =
        state.editableCards.isNotEmpty() && currentEditableCard.card.isNotInPlayer()

    private fun Card.isNotInPlayer(): Boolean =
        id !in player.state.playingCards.map { it.card.id }
}