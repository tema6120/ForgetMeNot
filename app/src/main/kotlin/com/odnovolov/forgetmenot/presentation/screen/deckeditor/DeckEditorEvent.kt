package com.odnovolov.forgetmenot.presentation.screen.deckeditor

sealed class DeckEditorEvent {
    object RenameDeckButtonClicked : DeckEditorEvent()
    class RenameDeckDialogTextChanged(val text: String) : DeckEditorEvent()
    object RenameDeckDialogPositiveButtonClicked : DeckEditorEvent()
}