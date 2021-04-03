package com.odnovolov.forgetmenot.presentation.screen.grading

import com.odnovolov.forgetmenot.domain.interactor.decksettings.GradingSettings
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope

class GradingDiScope {
    // todo
    private val screenState = GradingScreenState(tip = null)

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
        override fun recreateDiScope() = GradingDiScope()

        override fun onCloseDiScope(diScope: GradingDiScope) {
            diScope.controller.dispose()
        }
    }
}