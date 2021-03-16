package com.odnovolov.forgetmenot.presentation.screen.cardappearance

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceEvent.*

class CardAppearanceController(
    private val cardAppearance: CardAppearance,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<CardAppearanceEvent, Nothing>() {
    override fun handle(event: CardAppearanceEvent) {
        when (event) {
            AlignQuestionToEdgeButtonClicked -> {
                cardAppearance.questionTextAlignment = CardTextAlignment.Edge
            }

            AlignQuestionToCenterButtonClicked -> {
                cardAppearance.questionTextAlignment = CardTextAlignment.Center
            }

            is QuestionTextSizeTextChanged -> {
                cardAppearance.questionTextSize = event.text.toIntOrNull() ?: return
            }

            AlignAnswerToEdgeButtonClicked -> {
                cardAppearance.answerTextAlignment = CardTextAlignment.Edge
            }

            AlignAnswerToCenterButtonClicked -> {
                cardAppearance.answerTextAlignment = CardTextAlignment.Center
            }

            is AnswerTextSizeTextChanged -> {
                cardAppearance.answerTextSize = event.text.toIntOrNull() ?: return
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}