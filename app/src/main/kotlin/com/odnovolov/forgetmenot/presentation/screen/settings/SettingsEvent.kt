package com.odnovolov.forgetmenot.presentation.screen.settings

sealed class SettingsEvent {
    object WalkingModeSettingsButton : SettingsEvent()
    object FullscreenInHomeAndSettingsCheckboxClicked : SettingsEvent()
    object FullscreenInExerciseCheckboxClicked : SettingsEvent()
    object FullscreenInRepetitionCheckboxClicked : SettingsEvent()
}