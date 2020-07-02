package com.odnovolov.forgetmenot.presentation.screen.decksetup

sealed class DeckSetupEvent {
    object RenameDeckButtonClicked : DeckSetupEvent()
    class RenameDeckDialogTextChanged(val text: String) : DeckSetupEvent()
    object RenameDeckDialogPositiveButtonClicked : DeckSetupEvent()
}