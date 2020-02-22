package com.odnovolov.forgetmenot.presentation.screen.decksettings

sealed class DeckSettingsCommand {
    class SetRenameDeckDialogText(val text: String) : DeckSettingsCommand()
    class SetNamePresetDialogText(val text: String) : DeckSettingsCommand()
}