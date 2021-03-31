package com.odnovolov.forgetmenot.presentation.screen.settings

sealed class SettingsEvent {
    object FullscreenInExerciseCheckboxClicked : SettingsEvent()
    object FullscreenInRepetitionCheckboxClicked : SettingsEvent()
    object FullscreenInOtherPlacesCheckboxClicked : SettingsEvent()
    object CardAppearanceButtonClicked : SettingsEvent()
    object ExerciseButtonClicked : SettingsEvent()
    object WalkingModeButtonClicked : SettingsEvent()
}