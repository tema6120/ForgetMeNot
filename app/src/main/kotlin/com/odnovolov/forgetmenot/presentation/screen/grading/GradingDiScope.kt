package com.odnovolov.forgetmenot.presentation.screen.grading

import com.odnovolov.forgetmenot.domain.interactor.decksettings.GradingSettings
import com.odnovolov.forgetmenot.persistence.shortterm.GradingScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope

class GradingDiScope private constructor(
    initialScreenState: GradingScreenState? = null
) {
    private val screenStateProvider = GradingScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val screenState: GradingScreenState =
        initialScreenState ?: screenStateProvider.load()

    private val gradingSettings = GradingSettings(
        DeckSettingsDiScope.getOrRecreate().deckSettings
    )

    val controller = GradingController(
        gradingSettings,
        screenState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = GradingViewModel(
        DeckSettingsDiScope.getOrRecreate().deckSettings.state,
        screenState
    )

    companion object : DiScopeManager<GradingDiScope>() {
        fun create(initialScreenState: GradingScreenState) = GradingDiScope(initialScreenState)

        override fun recreateDiScope() = GradingDiScope()

        override fun onCloseDiScope(diScope: GradingDiScope) {
            diScope.controller.dispose()
        }
    }
}