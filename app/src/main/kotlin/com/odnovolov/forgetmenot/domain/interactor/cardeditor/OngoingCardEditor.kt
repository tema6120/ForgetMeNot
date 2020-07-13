package com.odnovolov.forgetmenot.domain.interactor.cardeditor

import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise

class OngoingCardEditor(
    val editableCard: EditableCard,
    private val exercise: Exercise?
) {
    init {
        require(if (exercise == null) true else isCurrentExerciseCard()) {
            "If exercise is opened, only current card can be edited"
        }
    }

    private fun isCurrentExerciseCard() =
        exercise!!.currentExerciseCard.base.card.id == editableCard.card.id

    fun setQuestion(question: String) {
        editableCard.question = question
    }

    fun setAnswer(answer: String) {
        editableCard.answer = answer
    }

    fun isCardEdited(): Boolean {
        return editableCard.question != editableCard.card.question
                || editableCard.answer != editableCard.card.answer
    }

    fun save() {
        require(editableCard.question.isNotBlank() && editableCard.answer.isNotBlank()) {
            "Neither question nor answer must not be blank"
        }

        if (editableCard.question != editableCard.card.question
            || editableCard.answer != editableCard.card.answer
        ) {
            editableCard.card.question = editableCard.question
            editableCard.card.answer = editableCard.answer
            exercise?.notifyCurrentCardChanged()
        }
    }
}