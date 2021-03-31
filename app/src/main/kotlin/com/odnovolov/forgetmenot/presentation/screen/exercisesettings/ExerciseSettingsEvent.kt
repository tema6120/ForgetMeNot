package com.odnovolov.forgetmenot.presentation.screen.exercisesettings

sealed class ExerciseSettingsEvent {
    object AlwaysShowCardFilterButtonClicked : ExerciseSettingsEvent()
    object ConditionallyShowCardFilterButtonClicked : ExerciseSettingsEvent()
    class CardsThresholdForFilterDialogInputTextChanged(val text: String) : ExerciseSettingsEvent()
    object CardsThresholdForShowingFilterDialogOkButtonClicked : ExerciseSettingsEvent()
    object NeverShowCardFilterButtonClicked : ExerciseSettingsEvent()
}