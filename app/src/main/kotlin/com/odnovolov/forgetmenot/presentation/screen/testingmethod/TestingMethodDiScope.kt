package com.odnovolov.forgetmenot.presentation.screen.testingmethod

import com.odnovolov.forgetmenot.persistence.shortterm.TestingMethodScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.exampleexercise.ExampleExerciseDiScope

class TestingMethodDiScope private constructor(
    initialScreenState: TestingMethodScreenState? = null
) {
    private val screenStateProvider = TestingMethodScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val screenState: TestingMethodScreenState =
        initialScreenState ?: screenStateProvider.load()

    val controller = TestingMethodController(
        DeckSettingsDiScope.getOrRecreate().deckSettings,
        ExampleExerciseDiScope.getOrRecreate().exercise,
        screenState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        screenStateProvider
    )

    val viewModel = TestingMethodViewModel(
        DeckSettingsDiScope.getOrRecreate().deckSettings.state,
        screenState
    )

    companion object : DiScopeManager<TestingMethodDiScope>() {
        fun create (initialScreenState: TestingMethodScreenState) =
            TestingMethodDiScope(initialScreenState)

        override fun recreateDiScope() = TestingMethodDiScope()

        override fun onCloseDiScope(diScope: TestingMethodDiScope) {
            diScope.controller.dispose()
        }
    }
}