package com.odnovolov.forgetmenot.presentation.screen.questiondisplay

import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.example.ExampleExerciseDiScope

class QuestionDisplayDiScope {
    val controller = QuestionDisplayController(
        DeckSettingsDiScope.get()!!.deckSettings,
        ExampleExerciseDiScope.get()!!.exercise,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = QuestionDisplayViewModel(
        DeckSettingsDiScope.get()!!.deckSettings.state
    )

    companion object : DiScopeManager<QuestionDisplayDiScope>() {
        override fun recreateDiScope() = QuestionDisplayDiScope()

        override fun onCloseDiScope(diScope: QuestionDisplayDiScope) {
            diScope.controller.dispose()
        }
    }
}