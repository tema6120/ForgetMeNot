package com.odnovolov.forgetmenot.screen.repetition.view

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.screen.repetition.view.RepetitionViewEvent.*

class RepetitionViewController : BaseController<RepetitionViewEvent, Nothing>() {
    private val queries: RepetitionViewControllerQueries = database.repetitionViewControllerQueries

    override fun handleEvent(event: RepetitionViewEvent) {
        when (event) {
            is NewPageBecameSelected -> {
                if (queries.getCurrentRepetitionCardId().executeAsOne() != event.id) {
                    queries.setCurrentRepetitionCardId(event.id)
                }
            }

            is ShowAnswerButtonClicked -> {
                queries.setIsAnsweredTrue(event.id)
            }
        }
    }
}