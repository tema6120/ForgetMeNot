package com.odnovolov.forgetmenot.presentation.screen.editcard

import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.editcard.EditCardController.Command
import com.odnovolov.forgetmenot.presentation.screen.editcard.EditCardController.Command.UpdateQuestionAndAnswer
import com.odnovolov.forgetmenot.presentation.screen.editcard.EditCardEvent.*

class EditCardController(
    private val editCardScreenState: EditCardScreenState,
    private val exercise: Exercise,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<EditCardEvent, Command>() {
    sealed class Command {
        object UpdateQuestionAndAnswer : Command()
    }

    override fun handle(event: EditCardEvent) {
        when (event) {
            is QuestionInputChanged -> {
                editCardScreenState.question = event.text
            }

            is AnswerInputChanged -> {
                editCardScreenState.answer = event.text
            }

            ReverseCardButtonClicked -> {
                with(editCardScreenState) {
                    val newAnswer = question
                    question = answer
                    answer = newAnswer
                }
                sendCommand(UpdateQuestionAndAnswer)
            }

            CancelButtonClicked -> {
                navigator.navigateUp()
            }

            AcceptButtonClicked -> {
                exercise.editCurrentCard(
                    newQuestion = editCardScreenState.question,
                    newAnswer = editCardScreenState.answer
                )
                navigator.navigateUp()
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}