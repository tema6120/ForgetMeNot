package com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val walkingModeSettingsModule = module {
    scope<WalkingModeSettingsViewModel> {
        scoped { WalkingModeSettingsController(walkingModePreference = get(), store = get()) }
        viewModel { WalkingModeSettingsViewModel(walkingModePreference = get()) }
    }
}

const val WALKING_MODE_SETTINGS_MODULE_SCOPE_ID = "WALKING_MODE_SETTINGS_MODULE_SCOPE_ID"