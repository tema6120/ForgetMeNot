package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.entity.AbstractDeck

sealed class HomeEvent {
    // Search:
    class SearchTextChanged(val searchText: String) : HomeEvent()

    // Filters:
    object DecksAvailableForExerciseCheckboxClicked : HomeEvent()

    // Sorting:
    object SortingDirectionButtonClicked : HomeEvent()
    class SortByButtonClicked(val criterion: DeckSorting.Criterion) : HomeEvent()

    // Deck item:
    class DeckButtonClicked(val deckId: Long) : HomeEvent()
    class DeckButtonLongClicked(val deckId: Long) : HomeEvent()
    class DeckSelectorClicked(val deckId: Long) : HomeEvent()

    // Deck options:
    class DeckOptionButtonClicked(val deckId: Long) : HomeEvent()
    object StartExerciseDeckOptionSelected : HomeEvent()
    object AutoplayDeckOptionSelected : HomeEvent()
    object RenameDeckOptionSelected : HomeEvent()
    object SetupDeckOptionSelected : HomeEvent()
    object EditCardsDeckOptionSelected : HomeEvent()
    object PinDeckOptionSelected : HomeEvent()
    object UnpinDeckOptionSelected : HomeEvent()
    object ExportDeckOptionSelected : HomeEvent()
    object MergeIntoDeckOptionSelected : HomeEvent()
    object RemoveDeckOptionSelected : HomeEvent()

    // Bottom buttons:
    object AutoplayButtonClicked : HomeEvent()
    object ExerciseButtonClicked : HomeEvent()

    // Card item:
    class FoundCardClicked(val cardId: Long) : HomeEvent()
    class FoundCardLongClicked(val cardId: Long) : HomeEvent()

    // Selection toolbar:
    object CancelledSelection : HomeEvent()
    object SelectAllSelectionToolbarButtonClicked : HomeEvent()
    object RemoveSelectionToolbarButtonClicked : HomeEvent()
    object MoreSelectionToolbarButtonClicked : HomeEvent()

    // Deck selection options
    object PinDeckSelectionOptionSelected : HomeEvent()
    object UnpinDeckSelectionOptionSelected : HomeEvent()
    object ExportDeckSelectionOptionSelected : HomeEvent()
    object MergeIntoDeckSelectionOptionSelected : HomeEvent()
    class DeckToMergeIntoIsSelected(val abstractDeck: AbstractDeck) : HomeEvent()
    object MergedDecksSnackbarCancelButtonClicked : HomeEvent()
    object RemoveDeckSelectionOptionSelected : HomeEvent()
    object RemovedDecksSnackbarCancelButtonClicked : HomeEvent()

    // Card selection options
    object InvertCardSelectionOptionSelected : HomeEvent()
    object ChangeGradeCardSelectionOptionSelected : HomeEvent()
    class SelectedGrade(val grade: Int) : HomeEvent()
    object MarkAsLearnedCardSelectionOptionSelected : HomeEvent()
    object MarkAsUnlearnedCardSelectionOptionSelected : HomeEvent()
    object RemoveCardsCardSelectionOptionSelected : HomeEvent()
    object MoveCardSelectionOptionSelected : HomeEvent()
    class DeckToMoveCardsToIsSelected(val abstractDeck: AbstractDeck) : HomeEvent()
    object CopyCardSelectionOptionSelected : HomeEvent()
    class DeckToCopyCardsToIsSelected(val abstractDeck: AbstractDeck) : HomeEvent()
    object CancelCardSelectionActionSnackbarButtonClicked : HomeEvent()
}