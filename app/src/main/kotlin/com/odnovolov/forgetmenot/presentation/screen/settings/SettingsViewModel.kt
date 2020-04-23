package com.odnovolov.forgetmenot.presentation.screen.settings

import com.odnovolov.forgetmenot.presentation.common.entity.FullscreenPreference
import kotlinx.coroutines.flow.Flow

class SettingsViewModel(fullscreenPreference: FullscreenPreference) {
    val fullscreenPreference: Flow<FullscreenPreference> = fullscreenPreference.asFlow()
}