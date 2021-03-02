package com.odnovolov.forgetmenot.presentation.screen.deckeditor

sealed class DeckEditorEvent {
    object RenameDeckButtonClicked : DeckEditorEvent()
    object AddCardButtonClicked : DeckEditorEvent()

    // Card selection toolbar
    object CancelledCardSelection : DeckEditorEvent()
    object SelectAllCardsButtonClicked : DeckEditorEvent()
    object RemoveCardsOptionSelected : DeckEditorEvent()
    object CancelSnackbarButtonClicked : DeckEditorEvent()
}