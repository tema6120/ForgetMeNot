package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings

sealed class RepetitionSettingsEvent {
    object StartRepetitionMenuItemClicked : RepetitionSettingsEvent()
    object AvailableForExerciseGroupButtonClicked : RepetitionSettingsEvent()
    object AwaitingGroupButtonClicked : RepetitionSettingsEvent()
    object LearnedGroupButtonClicked : RepetitionSettingsEvent()
    class GradeRangeChanged(val levelOfKnowledgeRange: IntRange) : RepetitionSettingsEvent()
    object LastAnswerFromButtonClicked : RepetitionSettingsEvent()
    object LastAnswerToButtonClicked : RepetitionSettingsEvent()
    object LapsButtonClicked : RepetitionSettingsEvent()
}