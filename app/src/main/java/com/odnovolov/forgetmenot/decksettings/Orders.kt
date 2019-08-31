package com.odnovolov.forgetmenot.decksettings

sealed class DeckSettingsOrder {
    object ShowRenameDeckDialog : DeckSettingsOrder()
    object NavigateToPronunciation : DeckSettingsOrder()
}