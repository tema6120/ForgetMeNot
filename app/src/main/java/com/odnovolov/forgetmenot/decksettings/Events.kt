package com.odnovolov.forgetmenot.decksettings

sealed class DeckSettingsEvent {
    object RenameDeckButtonClicked : DeckSettingsEvent()
    object RandomOrderSwitcherClicked : DeckSettingsEvent()
    object PronunciationButtonClicked : DeckSettingsEvent()
    object GotPronunciation : DeckSettingsEvent()
}