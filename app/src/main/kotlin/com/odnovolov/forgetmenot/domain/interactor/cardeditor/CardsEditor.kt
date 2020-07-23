package com.odnovolov.forgetmenot.domain.interactor.cardeditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState

abstract class CardsEditor(
    val state: State
) {
    class State(
        editableCards: List<EditableCard>,
        currentPosition: Int = 0
    ) : FlowableState<State>() {
        var editableCards: List<EditableCard> by me(editableCards)
        var currentPosition: Int by me(currentPosition)
    }

    val currentEditableCard: EditableCard get() = state.editableCards[state.currentPosition]

    fun setCurrentPosition(position: Int) {
        if (position !in 0..state.editableCards.lastIndex) return
        state.currentPosition = position
    }

    open fun setQuestion(question: String) {
        currentEditableCard.question = question
    }

    open fun setAnswer(answer: String) {
        currentEditableCard.answer = answer
    }

    open fun setIsLearned(isLearned: Boolean) {
        currentEditableCard.isLearned = isLearned
    }

    open fun setLevelOfKnowledge(levelOfKnowledge: Int) {
        currentEditableCard.levelOfKnowledge = levelOfKnowledge
    }

    abstract fun isCurrentCardRemovable(): Boolean
    abstract fun removeCard(): Boolean
    abstract fun restoreLastRemovedCard()
    abstract fun areCardsEdited(): Boolean
    abstract fun save(): SavingResult

    sealed class SavingResult {
        object Success : SavingResult()
        class Failure(val failureCause: FailureCause) : SavingResult()

        sealed class FailureCause {
            class HasUnderfilledCards(val positions: List<Int>) : FailureCause()
            object AllCardsAreEmpty : FailureCause()
        }
    }
}