package com.odnovolov.forgetmenot.presentation.screen.ongoingcardeditor

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.OngoingCardEditor
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor.SkeletalQAEditorController

class OngoingQAEditorController(
    private val ongoingCardEditor: OngoingCardEditor,
    longTermStateSaver: LongTermStateSaver
) : SkeletalQAEditorController(
    longTermStateSaver
) {
    override fun onQuestionInputChanged(text: String) {
        ongoingCardEditor.setQuestion(text)
    }

    override fun onAnswerInputChanged(text: String) {
        ongoingCardEditor.setAnswer(text)
    }
}