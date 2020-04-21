package com.odnovolov.forgetmenot.presentation.screen.repetition.view.repetitioncard

import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition.State
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.repetition.view.repetitioncard.RepetitionCardEvent.*

class RepetitionCardController(
    private val repetition: Repetition,
    private val longTermStateSaver: LongTermStateSaver,
    private val repetitionStateProvider: ShortTermStateProvider<State>
) : BaseController<RepetitionCardEvent, Nothing>() {
    override val autoSave = false

    override fun handle(event: RepetitionCardEvent) {
        when (event) {
            ShowQuestionButtonClicked -> {
                repetition.showQuestion()
                saveState()
            }

            ShowAnswerButtonClicked -> {
                repetition.showAnswer()
                saveState()
            }

            is QuestionTextSelectionChanged -> {
                repetition.setQuestionSelection(event.selection)
            }

            is AnswerTextSelectionChanged -> {
                repetition.setAnswerSelection(event.selection)
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        repetitionStateProvider.save(repetition.state)
    }
}