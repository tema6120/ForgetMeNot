package com.odnovolov.forgetmenot.presentation.screen.editcard

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class EditCardViewModel(
    private val editCardScreenState: EditCardScreenState
) {
    val question: String get() = editCardScreenState.questionInput

    val answer: String get() = editCardScreenState.answerInput

    val isAcceptButtonEnabled: Flow<Boolean> = combine(
        editCardScreenState.flowOf(EditCardScreenState::questionInput),
        editCardScreenState.flowOf(EditCardScreenState::answerInput)
    ) { question: String, answer: String ->
        question.isNotBlank() && answer.isNotBlank()
    }
}