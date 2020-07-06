package com.odnovolov.forgetmenot.presentation.screen.ongoingcardeditor

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class OngoingCardEditorViewModel(
    editableCard: EditableCard
) {
    val isAcceptButtonEnabled: Flow<Boolean> = combine(
        editableCard.flowOf(EditableCard::question),
        editableCard.flowOf(EditableCard::answer)
    ) { question: String, answer: String ->
        question.isNotBlank() && answer.isNotBlank()
    }
}