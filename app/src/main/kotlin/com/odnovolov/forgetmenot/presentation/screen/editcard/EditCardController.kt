package com.odnovolov.forgetmenot.presentation.screen.editcard

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardEditor
import com.odnovolov.forgetmenot.persistence.shortterm.EditCardScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.catchAndLogException
import com.odnovolov.forgetmenot.presentation.screen.editcard.EditCardController.Command
import com.odnovolov.forgetmenot.presentation.screen.editcard.EditCardController.Command.UpdateQuestionAndAnswer
import com.odnovolov.forgetmenot.presentation.screen.editcard.EditCardEvent.*

class EditCardController(
    private val screenState: EditCardScreenState,
    private val cardEditor: CardEditor,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val editCardScreenStateProvider: EditCardScreenStateProvider
) : BaseController<EditCardEvent, Command>() {
    sealed class Command {
        object UpdateQuestionAndAnswer : Command()
    }

    override fun handle(event: EditCardEvent) {
        when (event) {
            is QuestionInputChanged -> {
                screenState.questionInput = event.text
            }

            is AnswerInputChanged -> {
                screenState.answerInput = event.text
            }

            ReverseCardButtonClicked -> {
                with(screenState) {
                    val newAnswer = questionInput
                    questionInput = answerInput
                    answerInput = newAnswer
                }
                sendCommand(UpdateQuestionAndAnswer)
            }

            CancelButtonClicked -> {
                navigator.navigateUp()
            }

            AcceptButtonClicked -> {
                catchAndLogException {
                    cardEditor.updateCard(screenState.questionInput, screenState.answerInput)
                    navigator.navigateUp()
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        editCardScreenStateProvider.save(screenState)
    }
}