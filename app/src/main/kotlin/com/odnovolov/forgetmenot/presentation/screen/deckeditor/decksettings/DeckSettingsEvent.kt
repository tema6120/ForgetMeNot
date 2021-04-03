package com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings

sealed class DeckSettingsEvent {
    object RandomOrderSwitchToggled : DeckSettingsEvent()
    object PronunciationButtonClicked : DeckSettingsEvent()
    object CardInversionButtonClicked : DeckSettingsEvent()
    object QuestionDisplayButtonClicked : DeckSettingsEvent()
    object TestingMethodButtonClicked : DeckSettingsEvent()
    object IntervalsButtonClicked : DeckSettingsEvent()
    object GradingButtonClicked : DeckSettingsEvent()
    object MotivationalTimerButtonClicked : DeckSettingsEvent()
    object PronunciationPlanButtonClicked : DeckSettingsEvent()
}