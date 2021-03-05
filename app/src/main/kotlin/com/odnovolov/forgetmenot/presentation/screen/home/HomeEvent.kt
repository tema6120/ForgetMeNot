package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.entity.AbstractDeck
import com.odnovolov.forgetmenot.domain.interactor.searcher.FoundCard

sealed class HomeEvent {
    class SearchTextChanged(val searchText: String) : HomeEvent()

    // Selection toolbar:
    object CancelledSelection : HomeEvent()
    object SelectAllDecksButtonClicked : HomeEvent()
    object PinDeckSelectionOptionSelected : HomeEvent()
    object UnpinDeckSelectionOptionSelected : HomeEvent()
    object ExportDeckSelectionOptionSelected : HomeEvent()
    object MergeIntoDeckSelectionOptionSelected : HomeEvent()
    class DeckToMergeIntoIsSelected(val abstractDeck: AbstractDeck) : HomeEvent()
    object MergedDecksSnackbarCancelButtonClicked : HomeEvent()
    object RemoveDeckSelectionOptionSelected : HomeEvent()
    object RemovedDecksSnackbarCancelButtonClicked : HomeEvent()

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
    class FoundCardClicked(val foundCard: FoundCard) : HomeEvent()
}