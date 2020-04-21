package com.odnovolov.forgetmenot.domain.interactor.cardeditor

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise

class CardEditor(
    private val card: Card,
    private val exercise: Exercise?
) {
    init {
        checkCardIfExerciseIsOpened()
    }

    fun updateCard(newQuestion: String, newAnswer: String) {
        require(newQuestion.isNotBlank() && newAnswer.isNotBlank())
        checkCardIfExerciseIsOpened()

        if (newQuestion != card.question
            || newAnswer != card.answer
        ) {
            card.question = newQuestion
            card.answer = newAnswer
            exercise?.notifyCurrentCardChanged()
        }
    }

    private fun checkCardIfExerciseIsOpened() {
        require(exercise?.let { it.currentExerciseCard.base.card == card } ?: true) {
            "If exercise is opened, only current card can be edited"
        }
    }
}