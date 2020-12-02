package com.odnovolov.forgetmenot.domain.interactor.cardeditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableList
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.generateId

class CardsEditorForEditingExistingDeck(
    val deck: Deck,
    state: State
) : CardsEditorForEditingDeck(state) {
    override fun newEditableCard() = EditableCard(
        Card(generateId(), "", ""),
        deck
    )

    override fun areCardsEdited(): Boolean {
        with(state) {
            val originalCards = deck.cards
            if (originalCards.size != editableCards.size - 1) return true
            repeat(originalCards.size) { i ->
                val originalCard: Card = originalCards[i]
                val editableCard: EditableCard = editableCards[i]
                if (isEdited(originalCard, editableCard)) return true
            }
        }
        return false
    }

    private fun isEdited(originalCard: Card, editableCard: EditableCard): Boolean {
        return originalCard.id != editableCard.card.id
                || originalCard.question != editableCard.question
                || originalCard.answer != editableCard.answer
                || originalCard.isLearned != editableCard.isLearned
                || originalCard.grade != editableCard.levelOfKnowledge
    }

    override fun save(): SavingResult {
        checkDeck()?.let { failure -> return failure }
        val cards: CopyableList<Card> = applyChanges()
        deck.cards = cards
        return SavingResult.Success
    }
}