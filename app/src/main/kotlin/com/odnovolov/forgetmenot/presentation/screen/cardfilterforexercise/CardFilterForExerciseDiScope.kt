package com.odnovolov.forgetmenot.presentation.screen.cardfilterforexercise

import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class CardFilterForExerciseDiScope {
    val controller = CardFilterForExerciseController(
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = CardFilterForExerciseViewModel(

    )

    companion object : DiScopeManager<CardFilterForExerciseDiScope>() {
        override fun recreateDiScope() = CardFilterForExerciseDiScope()

        override fun onCloseDiScope(diScope: CardFilterForExerciseDiScope) {
            diScope.controller.dispose()
        }
    }
}