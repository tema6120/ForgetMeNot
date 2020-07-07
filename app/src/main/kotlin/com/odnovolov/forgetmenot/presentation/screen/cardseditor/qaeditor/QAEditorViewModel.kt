package com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import kotlinx.coroutines.flow.Flow

class QAEditorViewModel(private val editableCard: EditableCard) {
    val question: String get() = editableCard.question
    val answer: String get() = editableCard.answer
    val isLearned: Flow<Boolean> = editableCard.flowOf(EditableCard::isLearned)
}