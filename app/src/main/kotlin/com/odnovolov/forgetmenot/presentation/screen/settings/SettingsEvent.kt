package com.odnovolov.forgetmenot.presentation.screen.settings

sealed class SettingsEvent {
    object WalkingModeSettingsButtonClicked : SettingsEvent()
    object FullscreenInExerciseCheckboxClicked : SettingsEvent()
    object FullscreenInRepetitionCheckboxClicked : SettingsEvent()
    object FullscreenInOtherPlacesCheckboxClicked : SettingsEvent()
    object CardAppearanceButtonClicked : SettingsEvent()
}