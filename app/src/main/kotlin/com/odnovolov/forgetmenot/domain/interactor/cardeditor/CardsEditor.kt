package com.odnovolov.forgetmenot.domain.interactor.cardeditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.Deck

class CardsEditor(
    val state: State
) {
    class State(
        deck: Deck,
        editableCards: List<EditableCard> = deck.cards
            .map { card -> EditableCard(card) }
            .plus(EditableCard()),
        currentPosition: Int
    ) : FlowableState<State>() {
        val deck: Deck by me(deck)
        var editableCards: List<EditableCard> by me(editableCards)
        var currentPosition: Int by me(currentPosition)
    }

    val currentEditableCard: EditableCard get() = state.editableCards[state.currentPosition]
    private var cardBackup: CardBackup? = null

    fun setCurrentPosition(position: Int) {
        state.currentPosition = position
        ensureLastEmptyCard()
    }

    fun setQuestion(question: String) {
        currentEditableCard.question = question
        ensureLastEmptyCard()
    }

    fun setAnswer(answer: String) {
        currentEditableCard.answer = answer
        ensureLastEmptyCard()
    }

    private fun ensureLastEmptyCard() {
        with(state) {
            if (editableCards.last().isBlank()) {
                var redundantCardCount = 0
                for (i in editableCards.lastIndex - 1 downTo currentPosition) {
                    if (editableCards[i].isBlank()) {
                        redundantCardCount++
                    } else {
                        break
                    }
                }
                if (redundantCardCount > 0) {
                    editableCards = editableCards.dropLast(redundantCardCount)
                }
            } else {
                editableCards += EditableCard()
            }
        }
    }

    private fun EditableCard.isBlank(): Boolean = question.isBlank() && answer.isBlank()

    fun setIsLearned(isLearned: Boolean) {
        currentEditableCard.isLearned = isLearned
    }

    fun setLevelOfKnowledge(levelOfKnowledge: Int) {
        currentEditableCard.levelOfKnowledge = levelOfKnowledge
    }

    fun removeCard(): Boolean {
        with(state) {
            if (currentPosition == editableCards.lastIndex) return false
            cardBackup = CardBackup(editableCards[currentPosition], currentPosition)
            editableCards = editableCards.toMutableList().apply { removeAt(currentPosition) }
            return true
        }
    }

    fun restoreLastRemovedCard() {
        cardBackup?.let { cardBackup: CardBackup ->
            with(state) {
                // Because size of editableCards can be changed,
                // we correct insertPosition to avoid IndexOutOfBoundsException during inserting
                val insertPosition = minOf(cardBackup.position, editableCards.size)
                editableCards = editableCards.toMutableList().apply {
                    add(insertPosition, cardBackup.editableCard)
                }
            }
            ensureLastEmptyCard()
        }
        cardBackup = null
    }

    fun applyChanges(): Boolean {
        val hasIncompleteCard = state.editableCards.any(::isCardUnderfilled)
        if (hasIncompleteCard) return false
        state.deck.cards = state.editableCards
            .filter { editableCard -> editableCard.question.isNotBlank() }
            .map { editableCard ->
                editableCard.card.apply {
                    if (question != editableCard.question)
                        question = editableCard.question
                    if (answer != editableCard.answer)
                        answer = editableCard.answer
                    if (isLearned != editableCard.isLearned)
                        isLearned = editableCard.isLearned
                    if (levelOfKnowledge != editableCard.levelOfKnowledge)
                        levelOfKnowledge = editableCard.levelOfKnowledge
                }
            }
            .toCopyableList()
        return true
    }

    fun isCardUnderfilled(editableCard: EditableCard): Boolean {
        return editableCard.question.isBlank() xor editableCard.answer.isBlank()
    }

    private class CardBackup(
        val editableCard: EditableCard,
        val position: Int
    )
}