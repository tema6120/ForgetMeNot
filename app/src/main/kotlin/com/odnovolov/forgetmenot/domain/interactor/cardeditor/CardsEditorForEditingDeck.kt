package com.odnovolov.forgetmenot.domain.interactor.cardeditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableList
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.SavingResult.FailureCause.AllCardsAreEmpty
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.SavingResult.FailureCause.HasUnderfilledCards

abstract class CardsEditorForEditingDeck(
    state: State
) : CardsEditor(state) {
    private var cardBackup: CardBackup? = null

    init {
        ensureLastEmptyCard()
    }

    override fun setQuestion(question: String) {
        super.setQuestion(question)
        ensureLastEmptyCard()
    }

    override fun setAnswer(answer: String) {
        super.setAnswer(answer)
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
                editableCards = editableCards + newEditableCard()
            }
        }
    }

    protected abstract fun newEditableCard(): EditableCard

    override fun isCurrentCardRemovable(): Boolean {
        return state.currentPosition != state.editableCards.lastIndex
    }

    override fun removeCard(): Boolean {
        if (!isCurrentCardRemovable()) return false
        with(state) {
            cardBackup = CardBackup(
                editableCards[currentPosition],
                currentPosition
            )
            editableCards = editableCards.toMutableList().apply { removeAt(currentPosition) }
            return true
        }
    }

    override fun restoreLastRemovedCard() {
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

    protected fun checkDeck(): SavingResult.Failure? {
        return when {
            state.editableCards.all(EditableCard::isBlank) -> SavingResult.Failure(
                AllCardsAreEmpty
            )
            state.editableCards.any(EditableCard::isUnderfilled) -> {
                val positions: List<Int> = state.editableCards
                    .mapIndexedNotNull { index, editableCard ->
                        if (editableCard.isUnderfilled()) index else null
                    }
                SavingResult.Failure(
                    HasUnderfilledCards(positions)
                )
            }
            else -> null
        }
    }

    protected fun applyChanges(): CopyableList<Card> {
        return state.editableCards
            .filterNot(EditableCard::isBlank)
            .map { editableCard ->
                editableCard.card.apply {
                    question = editableCard.question
                    answer = editableCard.answer
                    isLearned = editableCard.isLearned
                    levelOfKnowledge = editableCard.levelOfKnowledge
                }
            }
            .toCopyableList()
    }

    private class CardBackup(
        val editableCard: EditableCard,
        val position: Int
    )
}