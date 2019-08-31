package com.odnovolov.forgetmenot.home

sealed class HomeEvent {
    class SearchTextChanged(val searchText: String) : HomeEvent()
    class DeckButtonClicked(val deckId: Long) : HomeEvent()
    class SetupDeckMenuItemClicked(val deckId: Long) : HomeEvent()
    class DeleteDeckMenuItemClicked(val deckId: Long) : HomeEvent()
    object DeckIsDeletedSnackbarCancelActionClicked : HomeEvent()
}