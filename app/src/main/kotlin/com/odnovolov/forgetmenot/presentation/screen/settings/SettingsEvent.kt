package com.odnovolov.forgetmenot.presentation.screen.settings

sealed class SettingsEvent {
    object WalkingModeSettingsButtonClicked : SettingsEvent()
    object FullscreenInHomeAndSettingsCheckboxClicked : SettingsEvent()
    object FullscreenInExerciseCheckboxClicked : SettingsEvent()
    object FullscreenInRepetitionCheckboxClicked : SettingsEvent()
}