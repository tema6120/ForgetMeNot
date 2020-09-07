package com.odnovolov.forgetmenot.presentation.screen.decksetup.decksettings

import com.odnovolov.forgetmenot.domain.entity.CardReverse
import com.odnovolov.forgetmenot.domain.entity.TestMethod

sealed class DeckSettingsEvent {
    object RandomOrderSwitchToggled : DeckSettingsEvent()
    class SelectedTestMethod(val testMethod: TestMethod) : DeckSettingsEvent()
    object IntervalsButtonClicked : DeckSettingsEvent()
    object PronunciationButtonClicked : DeckSettingsEvent()
    object DisplayQuestionSwitchToggled : DeckSettingsEvent()
    class SelectedCardReverse(val cardReverse: CardReverse) : DeckSettingsEvent()
    object SpeakPlanButtonClicked : DeckSettingsEvent()
    object TimeForAnswerButtonClicked : DeckSettingsEvent()
    object TestMethodHelpButtonClicked : DeckSettingsEvent()
    object IntervalsHelpButtonClicked : DeckSettingsEvent()
    object PronunciationHelpButtonClicked : DeckSettingsEvent()
    object QuestionDisplayHelpButtonClicked : DeckSettingsEvent()
    object SpeakPlanHelpButtonClicked : DeckSettingsEvent()
    object MotivationalTimerHelpButtonClicked : DeckSettingsEvent()
}