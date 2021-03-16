package com.odnovolov.forgetmenot.presentation.screen.cardappearance

import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearance.Companion.DEFAULT_ANSWER_TEXT_ALIGNMENT
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearance.Companion.DEFAULT_ANSWER_TEXT_SIZE
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearance.Companion.DEFAULT_QUESTION_TEXT_ALIGNMENT
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearance.Companion.DEFAULT_QUESTION_TEXT_SIZE

class CardAppearanceDiScope {
    private val cardAppearance = CardAppearance(
        questionTextAlignment = DEFAULT_QUESTION_TEXT_ALIGNMENT,
        questionTextSize = DEFAULT_QUESTION_TEXT_SIZE,
        answerTextAlignment = DEFAULT_ANSWER_TEXT_ALIGNMENT,
        answerTextSize = DEFAULT_ANSWER_TEXT_SIZE
    )

    val controller = CardAppearanceController(
        cardAppearance,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = CardAppearanceViewModel(
        cardAppearance
    )

    companion object : DiScopeManager<CardAppearanceDiScope>() {
        override fun recreateDiScope() = CardAppearanceDiScope()

        override fun onCloseDiScope(diScope: CardAppearanceDiScope) {
            diScope.controller.dispose()
        }
    }
}