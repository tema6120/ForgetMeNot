package com.odnovolov.forgetmenot.decksettings

sealed class DeckSettingsOrder {
    object ShowRenameDeckDialog : DeckSettingsOrder()
    class SetDialogText(val text: String) : DeckSettingsOrder()
    object NavigateToPronunciation : DeckSettingsOrder()
}