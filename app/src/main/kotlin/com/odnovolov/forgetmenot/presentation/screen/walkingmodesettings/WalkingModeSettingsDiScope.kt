package com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings

import com.odnovolov.forgetmenot.persistence.longterm.walkingmodepreference.WalkingModePreferenceProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class WalkingModeSettingsDiScope {
    private val walkingModePreferenceProvider
        get() = WalkingModePreferenceProvider(
            AppDiScope.get().database
        )

    private val walkingModePreference: WalkingModePreference = walkingModePreferenceProvider.load()

    val controller = WalkingModeSettingsController(
        walkingModePreference,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = WalkingModeSettingsViewModel(
        walkingModePreference
    )

    companion object : DiScopeManager<WalkingModeSettingsDiScope>() {
        override fun recreateDiScope() = WalkingModeSettingsDiScope()

        override fun onCloseDiScope(diScope: WalkingModeSettingsDiScope) {
            diScope.controller.dispose()
        }
    }
}