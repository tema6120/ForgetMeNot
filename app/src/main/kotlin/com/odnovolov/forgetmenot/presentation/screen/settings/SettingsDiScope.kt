package com.odnovolov.forgetmenot.presentation.screen.settings

import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivityDiScope

class SettingsDiScope {
    val controller = SettingsController(
        AppDiScope.get().globalState,
        AppDiScope.get().navigator,
        MainActivityDiScope.getOrRecreate().fullScreenPreference,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = SettingsViewModel(
        MainActivityDiScope.getOrRecreate().fullScreenPreference
    )

    companion object : DiScopeManager<SettingsDiScope>() {
        override fun recreateDiScope() = SettingsDiScope()

        override fun onCloseDiScope(diScope: SettingsDiScope) {
            diScope.controller.dispose()
        }
    }
}