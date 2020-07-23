package com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor.QAEditorEvent.AnswerInputChanged
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor.QAEditorEvent.QuestionInputChanged

class QAEditorController(
    private val cardId: Long,
    private val cardsEditor: CardsEditor,
    private val longTermStateSaver: LongTermStateSaver,
    private val cardsEditorProvider: ShortTermStateProvider<CardsEditor>
) : BaseController<QAEditorEvent, Nothing>() {
    override fun handle(event: QAEditorEvent) {
        when (event) {
            is QuestionInputChanged -> {
                if (cardsEditor.currentEditableCard.card.id != cardId) return
                cardsEditor.setQuestion(event.text)
            }

            is AnswerInputChanged -> {
                if (cardsEditor.currentEditableCard.card.id != cardId) return
                cardsEditor.setAnswer(event.text)
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        cardsEditorProvider.save(cardsEditor)
    }
}