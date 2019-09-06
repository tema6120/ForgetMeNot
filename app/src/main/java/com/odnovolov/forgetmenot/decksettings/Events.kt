package com.odnovolov.forgetmenot.decksettings

sealed class DeckSettingsEvent {
    object RenameDeckButtonClicked : DeckSettingsEvent()
    object RandomOrderSwitchToggled : DeckSettingsEvent()
    object PronunciationButtonClicked : DeckSettingsEvent()
}