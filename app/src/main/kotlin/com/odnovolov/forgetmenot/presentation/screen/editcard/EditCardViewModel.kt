package com.odnovolov.forgetmenot.presentation.screen.editcard

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.KoinComponent

class EditCardViewModel(
    private val editCardScreenState: EditCardScreenState
) : ViewModel(), KoinComponent {
    val question: String
        get() = editCardScreenState.question

    val answer: String
        get() = editCardScreenState.answer

    val isDoneButtonEnabled: Flow<Boolean> = combine(
        editCardScreenState.flowOf(EditCardScreenState::question),
        editCardScreenState.flowOf(EditCardScreenState::answer)
    ) { question: String, answer: String ->
        question.isNotEmpty() && answer.isNotEmpty()
    }

    override fun onCleared() {
        getKoin().getScope(EDIT_CARD_SCOPE_ID).close()
    }
}