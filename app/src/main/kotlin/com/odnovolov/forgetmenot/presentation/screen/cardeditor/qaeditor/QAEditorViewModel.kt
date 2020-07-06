package com.odnovolov.forgetmenot.presentation.screen.cardeditor.qaeditor

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard

class QAEditorViewModel(private val editableCard: EditableCard) {
    val question: String get() = editableCard.question
    val answer: String get() = editableCard.answer
}