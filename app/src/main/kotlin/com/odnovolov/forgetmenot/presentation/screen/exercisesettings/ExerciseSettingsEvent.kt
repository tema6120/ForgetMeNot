package com.odnovolov.forgetmenot.presentation.screen.exercisesettings

sealed class ExerciseSettingsEvent {
    object DoNotFilterButtonClicked : ExerciseSettingsEvent()
    object LimitCardsButtonClicked : ExerciseSettingsEvent()
    object ConditionallyShowCardFilterButtonClicked : ExerciseSettingsEvent()
    object AlwaysShowCardFilterButtonClicked : ExerciseSettingsEvent()
    class CardsThresholdDialogInputTextChanged(val text: String) : ExerciseSettingsEvent()
    object CardsThresholdDialogOkButtonClicked : ExerciseSettingsEvent()
    object ShowProgressBarButtonClicked : ExerciseSettingsEvent()
    object ShowTextOfCardPositionButtonClicked : ExerciseSettingsEvent()
    object YesVibrateOnWrongAnswerButtonClicked : ExerciseSettingsEvent()
    object NoVibrateOnWrongAnswerButtonClicked : ExerciseSettingsEvent()
    object YesGoToNextCardButtonClicked : ExerciseSettingsEvent()
    object NoGoToNextCardButtonClicked : ExerciseSettingsEvent()
}