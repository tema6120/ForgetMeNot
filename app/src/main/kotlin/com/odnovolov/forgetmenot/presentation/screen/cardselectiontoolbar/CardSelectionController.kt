package com.odnovolov.forgetmenot.presentation.screen.cardselectiontoolbar

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.BatchCardEditor
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardselectiontoolbar.CardSelectionEvent.*

class CardSelectionController(
    private val batchCardEditor: BatchCardEditor,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<CardSelectionEvent, Nothing>() {
    override fun handle(event: CardSelectionEvent) {
        when (event) {
            InvertOptionSelected -> {

            }

            ChangeGradeOptionSelected -> {

            }

            MarkAsLearnedOptionSelected -> {

            }

            MarkAsUnlearnedOptionSelected -> {

            }

            RemoveCardsOptionSelected -> {

            }

            MoveOptionSelected -> {

            }

            CopyOptionSelected -> {

            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}