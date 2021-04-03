package com.odnovolov.forgetmenot.presentation.screen.grading

import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope

class GradingDiScope {
    val controller = GradingController(
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = GradingViewModel(
        DeckSettingsDiScope.getOrRecreate().deckSettings.state
    )

    companion object : DiScopeManager<GradingDiScope>() {
        override fun recreateDiScope() = GradingDiScope()

        override fun onCloseDiScope(diScope: GradingDiScope) {
            diScope.controller.dispose()
        }
    }
}