package com.odnovolov.forgetmenot.editcard

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.editcard.EditCardEvent.*
import com.odnovolov.forgetmenot.editcard.EditCardOrder.*

class EditCardController : BaseController<EditCardEvent, EditCardOrder>() {
    private val queries: EditCardControllerQueries = database.editCardControllerQueries

    override fun handleEvent(event: EditCardEvent) {
        when (event) {
            is QuestionInputChanged -> {
                event.text?.let { queries.setQuestion(it.toString()) }
            }

            is AnswerInputChanged -> {
                event.text?.let { queries.setAnswer(it.toString()) }
            }

            ReverseCardButtonClicked -> {
                queries.reverseQuestionAndAnswer()
                issueOrder(UpdateQuestionAndAnswer)
            }

            CancelButtonClicked -> {
                issueOrder(NavigateUp)
            }

            DoneButtonClicked -> {
                queries.updateCard()
                issueOrder(NavigateUp)
            }
        }
    }
}