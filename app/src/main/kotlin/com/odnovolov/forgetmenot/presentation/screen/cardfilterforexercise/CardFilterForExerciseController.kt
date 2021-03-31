package com.odnovolov.forgetmenot.presentation.screen.cardfilterforexercise

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController

class CardFilterForExerciseController(
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<CardFilterForExerciseEvent, Nothing>() {
    override fun handle(event: CardFilterForExerciseEvent) {
        when (event) {

        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}