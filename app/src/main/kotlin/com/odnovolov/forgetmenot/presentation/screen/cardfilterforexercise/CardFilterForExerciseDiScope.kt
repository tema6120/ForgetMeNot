package com.odnovolov.forgetmenot.presentation.screen.cardfilterforexercise

import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseStateCreatorWithFiltering
import com.odnovolov.forgetmenot.persistence.shortterm.ExerciseStateCreatorWithFilteringStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class CardFilterForExerciseDiScope private constructor(
    initialExerciseCreatorState: ExerciseStateCreatorWithFiltering.State? = null
) {
    private val exerciseCreatorStateProvider = ExerciseStateCreatorWithFilteringStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState
    )

    private val exerciseCreatorState: ExerciseStateCreatorWithFiltering.State =
        initialExerciseCreatorState ?: exerciseCreatorStateProvider.load()

    private val exerciseStateCreator = ExerciseStateCreatorWithFiltering(
        exerciseCreatorState,
        AppDiScope.get().globalState
    )

    val controller = CardFilterForExerciseController(
        exerciseStateCreator,
        AppDiScope.get().globalState.cardFilterForExercise,
        AppDiScope.get().navigator,
        AppDiScope.get().globalState,
        AppDiScope.get().longTermStateSaver,
        exerciseCreatorStateProvider
    )

    val viewModel = CardFilterForExerciseViewModel(
        exerciseStateCreator,
        AppDiScope.get().globalState
    )

    companion object : DiScopeManager<CardFilterForExerciseDiScope>() {
        fun create(
            exerciseCreatorState: ExerciseStateCreatorWithFiltering.State
        ) = CardFilterForExerciseDiScope(
            exerciseCreatorState
        )

        override fun recreateDiScope() = CardFilterForExerciseDiScope()

        override fun onCloseDiScope(diScope: CardFilterForExerciseDiScope) {
            diScope.controller.dispose()
        }
    }
}