package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.interactor.searcher.SearchCard
import java.io.OutputStream

sealed class HomeEvent {
    class SearchTextChanged(val searchText: String) : HomeEvent()
    class FileForExportDeckIsReady(val outputStream: OutputStream) : HomeEvent()
    object DecksRemovedSnackbarCancelButtonClicked : HomeEvent()

    // Selection toolbar:
    object SelectionCancelled : HomeEvent()
    object SelectAllDecksButtonClicked : HomeEvent()
    object RemoveDecksButtonClicked : HomeEvent()

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
    class StartExerciseDeckOptionSelected(val deckId: Long) : HomeEvent()
    class AutoplayDeckOptionSelected(val deckId: Long) : HomeEvent()
    class ShowCardsDeckOptionSelected(val deckId: Long) : HomeEvent()
    class SetupDeckOptionSelected(val deckId: Long) : HomeEvent()
    class ExportDeckOptionSelected(val deckId: Long) : HomeEvent()
    class RemoveDeckOptionSelected(val deckId: Long) : HomeEvent()

    // Bottom buttons:
    object AutoplayButtonClicked : HomeEvent()
    object ExerciseButtonClicked : HomeEvent()

    // Card item:
    class FoundCardClicked(val searchCard: SearchCard) : HomeEvent()
}