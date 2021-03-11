package com.odnovolov.forgetmenot.presentation.screen.decklistseditor

sealed class DeckListsEditorEvent {
    class NewDeckListNameChanged(val name: String) : DeckListsEditorEvent()
    object SaveNewDeckListButtonClicked : DeckListsEditorEvent()
    class DeckListNameChanged(val name: String, val deckListId: Long) : DeckListsEditorEvent()
    class RemoveDeckListButtonClicked(val deckListId: Long) : DeckListsEditorEvent()
    object DoneButtonClicked : DeckListsEditorEvent()
}