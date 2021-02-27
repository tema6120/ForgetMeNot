package com.odnovolov.forgetmenot.domain.interactor.cardeditor

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.SavingResult.Success
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise

class CardsEditorForExercise(
    private val exercise: Exercise,
    removedCards: MutableList<EditableCard> = ArrayList(),
    state: State
) : CardsEditorForEditingSpecificCards(
    removedCards,
    state
) {
    private fun Card.isInExercise(): Boolean =
        id in exercise.state.exerciseCards.map { it.base.card.id }

    override fun save(): SavingResult {
        check()?.let { failure -> return failure }
        reallyRemoveCards()
        applyChanges()
        return Success
    }

    private fun applyChanges() {
        val removedCardIds: List<Long> =
            removedCards.map { editableCard: EditableCard -> editableCard.card.id }
        exercise.notifyCardsRemoved(removedCardIds)
        state.editableCards.forEach { editableCard: EditableCard ->
            val originalCard = editableCard.card
            var isQuestionChanged = false
            var isAnswerChanged = false
            var isGradeChanged = false
            var isIsLearnedChanged = false
            if (editableCard.question != originalCard.question) {
                originalCard.question = editableCard.question
                isQuestionChanged = true
            }
            if (editableCard.answer != originalCard.answer) {
                originalCard.answer = editableCard.answer
                isAnswerChanged = true
            }
            if (editableCard.grade != originalCard.grade) {
                originalCard.grade = editableCard.grade
                isGradeChanged = true
            }
            if (editableCard.isLearned != originalCard.isLearned) {
                originalCard.isLearned = editableCard.isLearned
                isIsLearnedChanged = true
            }
            val isCardChanged: Boolean = isQuestionChanged || isAnswerChanged
                    || isGradeChanged || isIsLearnedChanged
            if (isCardChanged && originalCard.isInExercise()) {
                exercise.notifyCardChanged(
                    originalCard,
                    isQuestionChanged,
                    isAnswerChanged,
                    isGradeChanged,
                    isIsLearnedChanged
                )
            }
        }
    }
}