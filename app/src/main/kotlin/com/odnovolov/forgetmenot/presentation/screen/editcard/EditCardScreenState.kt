package com.odnovolov.forgetmenot.presentation.screen.editcard

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import com.odnovolov.forgetmenot.domain.entity.Card

class EditCardScreenState(
    card: Card,
    isExerciseOpened: Boolean,
    questionInput: String = card.question,
    answerInput: String = card.answer
) : FlowableState<EditCardScreenState>() {
    val card: Card by me(card)
    val isExerciseOpened: Boolean by me(isExerciseOpened)
    var questionInput: String by me(questionInput)
    var answerInput: String by me(answerInput)
}