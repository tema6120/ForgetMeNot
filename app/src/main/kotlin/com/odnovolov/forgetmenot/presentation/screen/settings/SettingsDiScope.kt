package com.odnovolov.forgetmenot.presentation.screen.settings

import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.common.di.MainActivityDiScope

class SettingsDiScope {
    val controller = SettingsController(
        AppDiScope.get().navigator,
        MainActivityDiScope.shareFullScreenPreference(),
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = SettingsViewModel(
        MainActivityDiScope.shareFullScreenPreference()
    )

    companion object : DiScopeManager<SettingsDiScope>() {
        override fun recreateDiScope() = SettingsDiScope()

        override fun onCloseDiScope(diScope: SettingsDiScope) {
            diScope.controller.dispose()
        }
    }
}