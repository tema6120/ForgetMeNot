package com.odnovolov.forgetmenot.presentation.screen.editcard

import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.screen.editcard.EditCardCommand.UpdateQuestionAndAnswer

class EditCardController(
    private val editCardScreenState: EditCardScreenState,
    private val exercise: Exercise,
    private val navigator: Navigator
) {
    private val commandFlow = EventFlow<EditCardCommand>()
    val commands = commandFlow.get()

    fun onQuestionInputChanged(text: String) {
        editCardScreenState.question = text
    }

    fun onAnswerInputChanged(text: String) {
        editCardScreenState.answer = text
    }

    fun onReverseCardButtonClicked() {
        with (editCardScreenState) {
            val newAnswer = question
            question = answer
            answer = newAnswer
        }
        commandFlow.send(UpdateQuestionAndAnswer)
    }

    fun onCancelButtonClicked() {
        navigator.navigateUp()
    }

    fun onDoneButtonClicked() {
        exercise.editCurrentCard(
            newQuestion = editCardScreenState.question,
            newAnswer = editCardScreenState.answer
        )
        navigator.navigateUp()
    }
}