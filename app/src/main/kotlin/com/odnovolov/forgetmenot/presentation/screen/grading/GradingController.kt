package com.odnovolov.forgetmenot.presentation.screen.grading

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.grading.GradingEvent.*

class GradingController(
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<GradingEvent, Nothing>() {
    override fun handle(event: GradingEvent) {
        when (event) {
            FirstCorrectAnswerButton -> {

            }

            FirstWrongAnswerButton -> {

            }

            YesAskAgainButton -> {

            }

            NoAskAgainButton -> {

            }

            RepeatedCorrectAnswerButton -> {

            }

            RepeatedWrongAnswerButton -> {

            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}