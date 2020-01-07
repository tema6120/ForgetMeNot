package com.odnovolov.forgetmenot.screen.home

sealed class HomeEvent {
    class SearchTextChanged(val searchText: String) : HomeEvent()
    object DisplayOnlyWithTasksCheckboxClicked : HomeEvent()
    class DeckButtonClicked(val deckId: Long) : HomeEvent()
    class DeckButtonLongClicked(val deckId: Long) : HomeEvent()
    class SetupDeckMenuItemClicked(val deckId: Long) : HomeEvent()
    class DeleteDeckMenuItemClicked(val deckId: Long) : HomeEvent()
    object DeckIsDeletedSnackbarCancelActionClicked : HomeEvent()
    object StartExerciseMenuItemClicked : HomeEvent()
    class SelectAllDecksMenuItemClicked(val displayedCardIds: List<Long>) : HomeEvent()
    object ActionModeFinished : HomeEvent()
}