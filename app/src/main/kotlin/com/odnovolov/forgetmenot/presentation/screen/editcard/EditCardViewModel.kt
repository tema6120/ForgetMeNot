package com.odnovolov.forgetmenot.presentation.screen.editcard

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class EditCardViewModel(
    private val editCardScreenState: EditCardScreenState
) {
    val question: String get() = editCardScreenState.question

    val answer: String get() = editCardScreenState.answer

    val isAcceptButtonEnabled: Flow<Boolean> = combine(
        editCardScreenState.flowOf(EditCardScreenState::question),
        editCardScreenState.flowOf(EditCardScreenState::answer)
    ) { question: String, answer: String ->
        question.isNotEmpty() && answer.isNotEmpty()
    }
}