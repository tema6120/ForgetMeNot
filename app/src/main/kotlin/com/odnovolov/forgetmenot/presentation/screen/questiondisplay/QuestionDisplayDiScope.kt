package com.odnovolov.forgetmenot.presentation.screen.questiondisplay

import com.odnovolov.forgetmenot.persistence.shortterm.QuestionDisplayScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.exampleexercise.ExampleExerciseDiScope

class QuestionDisplayDiScope private constructor(
    initialScreenState: QuestionDisplayScreenState? = null
) {
    private val screenStateProvider = QuestionDisplayScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val screenState: QuestionDisplayScreenState =
        initialScreenState ?: screenStateProvider.load()

    val controller = QuestionDisplayController(
        DeckSettingsDiScope.getOrRecreate().deckSettings,
        ExampleExerciseDiScope.getOrRecreate().exercise,
        screenState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        screenStateProvider
    )

    val viewModel = QuestionDisplayViewModel(
        DeckSettingsDiScope.getOrRecreate().deckSettings.state,
        screenState
    )

    companion object : DiScopeManager<QuestionDisplayDiScope>() {
        fun create(initialScreenState: QuestionDisplayScreenState) =
            QuestionDisplayDiScope(initialScreenState)

        override fun recreateDiScope() = QuestionDisplayDiScope()

        override fun onCloseDiScope(diScope: QuestionDisplayDiScope) {
            diScope.controller.dispose()
        }
    }
}