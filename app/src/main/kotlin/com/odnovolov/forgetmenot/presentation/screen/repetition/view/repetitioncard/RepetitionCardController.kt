package com.odnovolov.forgetmenot.presentation.screen.repetition.view.repetitioncard

import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.repetition.view.repetitioncard.RepetitionCardEvent.ShowAnswerButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.repetition.view.repetitioncard.RepetitionCardEvent.ShowQuestionButtonClicked

class RepetitionCardController(
    private val repetition: Repetition,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<RepetitionCardEvent, Nothing>() {
    override fun handle(event: RepetitionCardEvent) {
        when (event) {
            ShowQuestionButtonClicked -> {
                repetition.showQuestion()
            }

            ShowAnswerButtonClicked -> {
                repetition.showAnswer()
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}