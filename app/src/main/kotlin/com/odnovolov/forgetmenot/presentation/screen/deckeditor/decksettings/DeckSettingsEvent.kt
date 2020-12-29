package com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings

sealed class DeckSettingsEvent {
    object RandomOrderSwitchToggled : DeckSettingsEvent()
    object IntervalsButtonClicked : DeckSettingsEvent()
    object PronunciationButtonClicked : DeckSettingsEvent()
    object QuestionDisplayButtonClicked : DeckSettingsEvent()
    object TestingMethodButtonClicked : DeckSettingsEvent()
    object CardInversionButtonClicked : DeckSettingsEvent()
    object PronunciationPlanButtonClicked : DeckSettingsEvent()
    object MotivationalTimerButtonClicked : DeckSettingsEvent()
}