package com.odnovolov.forgetmenot.presentation.screen.decksettings

import com.odnovolov.forgetmenot.domain.entity.CardReverse
import com.odnovolov.forgetmenot.domain.entity.TestMethod

sealed class DeckSettingsEvent {
    object RenameDeckButtonClicked : DeckSettingsEvent()
    class RenameDeckDialogTextChanged(val text: String) : DeckSettingsEvent()
    object RenameDeckDialogPositiveButtonClicked : DeckSettingsEvent()
    object RandomOrderSwitchToggled : DeckSettingsEvent()
    class SelectedTestMethod(val testMethod: TestMethod) : DeckSettingsEvent()
    object IntervalsButtonClicked : DeckSettingsEvent()
    object PronunciationButtonClicked : DeckSettingsEvent()
    object DisplayQuestionSwitchToggled : DeckSettingsEvent()
    class SelectedCardReverse(val cardReverse: CardReverse) : DeckSettingsEvent()
    object SpeakPlanButtonClicked : DeckSettingsEvent()
}