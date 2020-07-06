package com.odnovolov.forgetmenot.presentation.screen.cardeditor.qaeditor

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardeditor.qaeditor.QAEditorEvent.AnswerInputChanged
import com.odnovolov.forgetmenot.presentation.screen.cardeditor.qaeditor.QAEditorEvent.QuestionInputChanged

abstract class SkeletalQAEditorController(
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<QAEditorEvent, Nothing>() {
    override fun handle(event: QAEditorEvent) {
        when (event) {
            is QuestionInputChanged -> onQuestionInputChanged(event.text)
            is AnswerInputChanged -> onAnswerInputChanged(event.text)
        }
    }

    abstract fun onQuestionInputChanged(text: String)
    abstract fun onAnswerInputChanged(text: String)

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}