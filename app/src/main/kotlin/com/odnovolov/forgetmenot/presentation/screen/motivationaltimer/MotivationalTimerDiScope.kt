package com.odnovolov.forgetmenot.presentation.screen.motivationaltimer

import com.odnovolov.forgetmenot.persistence.shortterm.MotivationalTimerScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.exampleexercise.ExampleExerciseDiScope

class MotivationalTimerDiScope private constructor(
    initialScreenState: MotivationalTimerScreenState? = null
) {
    private val screenStateProvider = MotivationalTimerScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val screenState: MotivationalTimerScreenState =
        initialScreenState ?: screenStateProvider.load()

    val controller = MotivationalTimerController(
        DeckSettingsDiScope.get()!!.deckSettings,
        ExampleExerciseDiScope.get()!!.exercise,
        screenState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        screenStateProvider
    )

    val viewModel = MotivationalTimerViewModel(
        DeckSettingsDiScope.get()!!.deckSettings.state,
        screenState
    )

    companion object : DiScopeManager<MotivationalTimerDiScope>() {
        fun create(initialScreenState: MotivationalTimerScreenState) =
            MotivationalTimerDiScope(initialScreenState)

        override fun recreateDiScope() = MotivationalTimerDiScope()

        override fun onCloseDiScope(diScope: MotivationalTimerDiScope) {
            diScope.controller.dispose()
        }
    }
}