package com.odnovolov.forgetmenot.presentation.screen.testingmethod

import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.example.ExampleExerciseDiScope

class TestingMethodDiScope {
    val controller = TestingMethodController(
        DeckSettingsDiScope.get()!!.deckSettings,
        ExampleExerciseDiScope.get()!!.exercise,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = TestingMethodViewModel(
        DeckSettingsDiScope.get()!!.deckSettings.state
    )

    companion object : DiScopeManager<TestingMethodDiScope>() {
        override fun recreateDiScope() = TestingMethodDiScope()

        override fun onCloseDiScope(diScope: TestingMethodDiScope) {
            diScope.controller.dispose()
        }
    }
}