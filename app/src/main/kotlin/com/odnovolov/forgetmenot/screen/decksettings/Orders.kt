package com.odnovolov.forgetmenot.screen.decksettings

sealed class DeckSettingsOrder {
    object ShowRenameDeckDialog : DeckSettingsOrder()
    class SetDialogText(val text: String) : DeckSettingsOrder()
    object NavigateToIntervals : DeckSettingsOrder()
    object NavigateToPronunciation : DeckSettingsOrder()
}