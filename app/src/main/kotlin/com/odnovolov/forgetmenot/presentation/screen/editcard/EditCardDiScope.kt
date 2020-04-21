package com.odnovolov.forgetmenot.presentation.screen.editcard

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardEditor
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.persistence.shortterm.EditCardScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseDiScope

class EditCardDiScope private constructor(
    initialEditCardScreenState: EditCardScreenState? = null
) {
    private val screenStateProvider = EditCardScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState
    )

    private val screenState: EditCardScreenState =
        initialEditCardScreenState ?: screenStateProvider.load()

    private val exercise: Exercise?
        get() = if (screenState.isExerciseOpened) {
            ExerciseDiScope.shareExercise()
        } else {
            null
        }

    private val cardEditor = CardEditor(
        screenState.card,
        exercise
    )

    val controller = EditCardController(
        screenState,
        cardEditor,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        screenStateProvider
    )

    val viewModel = EditCardViewModel(
        screenState
    )

    companion object : DiScopeManager<EditCardDiScope>() {
        fun create(initialEditCardScreenState: EditCardScreenState) =
            EditCardDiScope(initialEditCardScreenState)

        override fun recreateDiScope() = EditCardDiScope()

        override fun onCloseDiScope(diScope: EditCardDiScope) {
            diScope.controller.dispose()
        }
    }
}