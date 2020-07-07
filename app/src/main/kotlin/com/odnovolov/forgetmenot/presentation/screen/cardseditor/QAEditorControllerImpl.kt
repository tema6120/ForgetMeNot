package com.odnovolov.forgetmenot.presentation.screen.cardseditor

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor.SkeletalQAEditorController

class QAEditorControllerImpl(
    private val cardId: Long,
    private val cardsEditor: CardsEditor,
    longTermStateSaver: LongTermStateSaver
) : SkeletalQAEditorController(
    longTermStateSaver
) {
    override fun onQuestionInputChanged(text: String) {
        if (cardsEditor.currentEditableCard.card.id != cardId) return
        cardsEditor.setQuestion(text)
    }

    override fun onAnswerInputChanged(text: String) {
        if (cardsEditor.currentEditableCard.card.id != cardId) return
        cardsEditor.setAnswer(text)
    }
}