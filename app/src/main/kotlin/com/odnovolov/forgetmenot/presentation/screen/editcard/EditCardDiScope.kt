package com.odnovolov.forgetmenot.presentation.screen.editcard

import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseDiScope

class EditCardDiScope(initialEditCardScreenState: EditCardScreenState) {
    private val editCardScreenState = initialEditCardScreenState

    val controller = EditCardController(
        editCardScreenState,
        ExerciseDiScope.shareExercise(),
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = EditCardViewModel(
        editCardScreenState
    )

    companion object : DiScopeManager<EditCardDiScope>() {
        override fun recreateDiScope() =
            EditCardDiScope(EditCardScreenState()) // EditTexts will restore state

        override fun onCloseDiScope(diScope: EditCardDiScope) {
            diScope.controller.dispose()
        }
    }
}