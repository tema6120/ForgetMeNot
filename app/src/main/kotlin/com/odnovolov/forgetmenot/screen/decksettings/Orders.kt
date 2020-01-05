package com.odnovolov.forgetmenot.screen.decksettings

sealed class DeckSettingsOrder {
    class SetRenameDeckDialogText(val text: String) : DeckSettingsOrder()
    class SetNamePresetDialogText(val text: String) : DeckSettingsOrder()
    object NavigateToIntervals : DeckSettingsOrder()
    object NavigateToPronunciation : DeckSettingsOrder()
}