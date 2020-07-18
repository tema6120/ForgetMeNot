package com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings

import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class WalkingModeSettingsDiScope {
    val controller = WalkingModeSettingsController(
        AppDiScope.get().walkingModePreference,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = WalkingModeSettingsViewModel(
        AppDiScope.get().walkingModePreference
    )

    companion object : DiScopeManager<WalkingModeSettingsDiScope>() {
        override fun recreateDiScope() = WalkingModeSettingsDiScope()

        override fun onCloseDiScope(diScope: WalkingModeSettingsDiScope) {
            diScope.controller.dispose()
        }
    }
}