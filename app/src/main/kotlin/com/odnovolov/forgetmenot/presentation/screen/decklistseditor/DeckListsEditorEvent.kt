package com.odnovolov.forgetmenot.presentation.screen.decklistseditor

sealed class DeckListsEditorEvent {
    class SelectDeckListColorButtonClicked(val deckListId: Long) : DeckListsEditorEvent()
    class ColorHexTextWasSelected(val text: String) : DeckListsEditorEvent()
    class ColorWasSelected(val color: Int) : DeckListsEditorEvent()
    class NewDeckListNameChanged(val name: String) : DeckListsEditorEvent()
    object SaveNewDeckListButtonClicked : DeckListsEditorEvent()
    class DeckListNameChanged(val name: String, val deckListId: Long) : DeckListsEditorEvent()
    class RemoveDeckListButtonClicked(val deckListId: Long) : DeckListsEditorEvent()
    object CancelDeckListRemovingButtonClicked : DeckListsEditorEvent()
    object BackButtonClicked : DeckListsEditorEvent()
    object DoneButtonClicked : DeckListsEditorEvent()
    object SaveButtonClicked : DeckListsEditorEvent()
    object UserConfirmedExit : DeckListsEditorEvent()
}