package com.odnovolov.forgetmenot.screen.home

sealed class HomeEvent {
    class SearchTextChanged(val searchText: String) : HomeEvent()
    object DisplayOnlyWithTasksCheckboxClicked : HomeEvent()
    class DeckButtonClicked(val deckId: Long) : HomeEvent()
    class DeckButtonLongClicked(val deckId: Long) : HomeEvent()
    class SetupDeckMenuItemClicked(val deckId: Long) : HomeEvent()
    class RemoveDeckMenuItemClicked(val deckId: Long) : HomeEvent()
    object DecksRemovedSnackbarCancelActionClicked : HomeEvent()
    object StartExerciseMenuItemClicked : HomeEvent()
    class SelectAllDecksMenuItemClicked(val displayedCardIds: List<Long>) : HomeEvent()
    object RemoveDecksMenuItemClicked : HomeEvent()
    object ActionModeFinished : HomeEvent()
}