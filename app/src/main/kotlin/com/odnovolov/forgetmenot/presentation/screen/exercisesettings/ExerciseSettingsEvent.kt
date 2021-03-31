package com.odnovolov.forgetmenot.presentation.screen.exercisesettings

sealed class ExerciseSettingsEvent {
    object NeverFilterCardsButtonClicked : ExerciseSettingsEvent()
    object LimitCardsToButtonClicked : ExerciseSettingsEvent()
    object ConditionallyShowCardFilterButtonClicked : ExerciseSettingsEvent()
    object AlwaysShowCardFilterButtonClicked : ExerciseSettingsEvent()
    class CardsThresholdDialogInputTextChanged(val text: String) : ExerciseSettingsEvent()
    object CardsThresholdDialogOkButtonClicked : ExerciseSettingsEvent()
}