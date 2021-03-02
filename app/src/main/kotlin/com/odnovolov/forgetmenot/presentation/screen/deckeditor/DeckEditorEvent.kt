package com.odnovolov.forgetmenot.presentation.screen.deckeditor

sealed class DeckEditorEvent {
    object RenameDeckButtonClicked : DeckEditorEvent()
    object AddCardButtonClicked : DeckEditorEvent()
    object CancelledCardSelection : DeckEditorEvent()
    object SelectAllCardsButtonClicked : DeckEditorEvent()
}