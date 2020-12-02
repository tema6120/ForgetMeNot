package com.odnovolov.forgetmenot.domain.interactor.cardeditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.SavingResult.FailureCause.HasUnderfilledCards
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.SavingResult.Success

open class CardsEditorForEditingSpecificCards(
    val removedCards: MutableList<EditableCard> = ArrayList(),
    state: State
) : CardsEditor(state) {
    override fun isCurrentCardRemovable(): Boolean {
        return state.editableCards.isNotEmpty()
    }

    override fun removeCard(): Boolean {
        with(state) {
            if (!isCurrentCardRemovable()) return false
            editableCards = editableCards.toMutableList().apply {
                val removedCard = removeAt(currentPosition)
                removedCards.add(removedCard)
            }
            currentPosition = when {
                editableCards.isEmpty() -> -1
                currentPosition > editableCards.lastIndex -> editableCards.lastIndex
                else -> currentPosition
            }
            return true
        }
    }

    override fun restoreLastRemovedCard() {
        if (removedCards.isEmpty()) return
        val lastRemovedCard: EditableCard = removedCards.removeAt(removedCards.lastIndex)
        state.editableCards = state.editableCards + lastRemovedCard
        state.currentPosition = state.editableCards.lastIndex
    }

    override fun areCardsEdited(): Boolean {
        if (removedCards.isNotEmpty()) return true
        return state.editableCards.any { editableCard: EditableCard ->
            val originalCard = editableCard.card
            originalCard.question != editableCard.question
                    || originalCard.answer != editableCard.answer
                    || originalCard.isLearned != editableCard.isLearned
                    || originalCard.grade != editableCard.levelOfKnowledge
        }
    }

    override fun save(): SavingResult {
        check()?.let { failure -> return failure }
        reallyRemoveCards()
        applyChanges()
        return Success
    }

    protected fun check(): SavingResult.Failure? {
        val underfilledPositions: List<Int> =
            state.editableCards.mapIndexedNotNull { index, editableCard ->
                if (editableCard.question.isBlank() || editableCard.answer.isBlank()) index
                else null
            }
        return if (underfilledPositions.isEmpty()) null
        else SavingResult.Failure(HasUnderfilledCards(underfilledPositions))
    }

    protected fun reallyRemoveCards() {
        removedCards.forEach { editableCard: EditableCard ->
            val deck = editableCard.deck!!
            deck.cards = deck.cards
                .filter { card: Card -> card.id != editableCard.card.id }
                .toCopyableList()
        }
    }

    private fun applyChanges() {
        state.editableCards.forEach { editableCard: EditableCard ->
            editableCard.card.apply {
                question = editableCard.question
                answer = editableCard.answer
                isLearned = editableCard.isLearned
                grade = editableCard.levelOfKnowledge
            }
        }
    }
}