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

    fun setCurrentPosition(position: Int) {
        state.currentPosition = position
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
        val isCurrentPositionLast = state.currentPosition == state.editableCards.lastIndex
        if (isCurrentPositionLast) {
            if (currentEditableCard.question.isNotBlank()
                || currentEditableCard.answer.isNotBlank()) {
                state.editableCards += EditableCard()
            }
            return
        }
        val isCurrentPositionSecondToLast =
            state.currentPosition == state.editableCards.lastIndex - 1
        if (isCurrentPositionSecondToLast
            && currentEditableCard.question.isBlank()
            && currentEditableCard.answer.isBlank()) {
            state.editableCards = state.editableCards.dropLast(1)
        }
    }

    fun setIsLearned(isLearned: Boolean) {
        currentEditableCard.isLearned = isLearned
    }

    fun setLevelOfKnowledge(levelOfKnowledge: Int) {

    }

    fun removeCard() {

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
}