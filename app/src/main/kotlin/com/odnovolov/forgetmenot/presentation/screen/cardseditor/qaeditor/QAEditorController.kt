package com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
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
                if (cannotEdit) return
                cardsEditor.setQuestion(event.text)
            }

            is AnswerInputChanged -> {
                if (cannotEdit) return
                cardsEditor.setAnswer(event.text)
            }
        }
    }

    private val cannotEdit: Boolean
        get() {
            val currentEditableCard: EditableCard? =
                with(cardsEditor.state) { editableCards.getOrNull(currentPosition) }
            return currentEditableCard?.card?.id != cardId
        }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        cardsEditorProvider.save(cardsEditor)
    }
}