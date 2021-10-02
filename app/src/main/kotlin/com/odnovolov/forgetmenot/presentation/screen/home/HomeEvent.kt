package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.entity.AbstractDeck

sealed class HomeEvent {
    // Search:
    class SearchTextChanged(val searchText: String) : HomeEvent()

    // Filters:
    object DecksAvailableForExerciseCheckboxClicked : HomeEvent()
    object EditDeckListsButtonClicked : HomeEvent()
    class DeckListWasSelected(val deckListId: Long?) : HomeEvent()
    object CreateDeckListButtonClicked : HomeEvent()

    // Sorting:
    object SortingDirectionButtonClicked : HomeEvent()
    class SortByButtonClicked(val criterion: DeckSorting.Criterion) : HomeEvent()
    object NewDecksFirstCheckboxClicked : HomeEvent()

    // Deck item:
    class DeckButtonClicked(val deckId: Long) : HomeEvent()
    class DeckButtonLongClicked(val deckId: Long) : HomeEvent()
    class DeckSelectorClicked(val deckId: Long) : HomeEvent()

    // Deck options:
    class DeckOptionButtonClicked(val deckId: Long) : HomeEvent()
    object StartExerciseDeckOptionWasSelected : HomeEvent()
    object AutoplayDeckOptionWasSelected : HomeEvent()
    object RenameDeckOptionWasSelected : HomeEvent()
    object SetupDeckOptionWasSelected : HomeEvent()
    object EditCardsDeckOptionWasSelected : HomeEvent()
    object PinDeckOptionWasSelected : HomeEvent()
    object UnpinDeckOptionWasSelected : HomeEvent()
    object AddToDeckListDeckOptionWasSelected : HomeEvent()
    object RemoveFromDeckListDeckOptionWasSelected : HomeEvent()
    object ExportDeckOptionWasSelected : HomeEvent()
    object MergeIntoDeckOptionWasSelected : HomeEvent()
    object RemoveDeckOptionWasSelected : HomeEvent()

    // Bottom buttons:
    object AutoplayButtonClicked : HomeEvent()
    object ExerciseButtonClicked : HomeEvent()

    // Card item:
    class FoundCardClicked(val cardId: Long) : HomeEvent()
    class FoundCardLongClicked(val cardId: Long) : HomeEvent()

    // Selection toolbar:
    object SelectionWasCancelled : HomeEvent()
    object SelectAllSelectionToolbarButtonClicked : HomeEvent()
    object RemoveSelectionToolbarButtonClicked : HomeEvent()
    object MoreSelectionToolbarButtonClicked : HomeEvent()

    // Deck selection options
    object PinDeckSelectionOptionWasSelected : HomeEvent()
    object UnpinDeckSelectionOptionWasSelected : HomeEvent()
    object AddToDeckListDeckSelectionOptionWasSelected : HomeEvent()
    object RemoveFromDeckListDeckSelectionOptionWasSelected : HomeEvent()
    object SetPresetDeckSelectionOptionWasSelected : HomeEvent()
    class PresetButtonClicked(val exercisePreferenceId: Long) : HomeEvent()
    object PresetHasBeenAppliedSnackbarCancelButtonClicked : HomeEvent()
    object ExportDeckSelectionOptionWasSelected : HomeEvent()
    object MergeIntoDeckSelectionOptionWasSelected : HomeEvent()
    class DeckToMergeIntoWasSelected(val abstractDeck: AbstractDeck) : HomeEvent()
    object MergedDecksSnackbarCancelButtonClicked : HomeEvent()
    object RemoveDeckSelectionOptionWasSelected : HomeEvent()
    object RemovedDecksSnackbarCancelButtonClicked : HomeEvent()

    // Card selection options
    object InvertCardSelectionOptionWasSelected : HomeEvent()
    object ChangeGradeCardSelectionOptionWasSelected : HomeEvent()
    class GradeWasSelected(val grade: Int) : HomeEvent()
    object MarkAsLearnedCardSelectionOptionWasSelected : HomeEvent()
    object MarkAsUnlearnedCardSelectionOptionWasSelected : HomeEvent()
    object RemoveCardsCardSelectionOptionWasSelected : HomeEvent()
    object MoveCardSelectionOptionWasSelected : HomeEvent()
    class DeckToMoveCardsToWasSelected(val abstractDeck: AbstractDeck) : HomeEvent()
    object CopyCardSelectionOptionWasSelected : HomeEvent()
    class DeckToCopyCardsToWasSelected(val abstractDeck: AbstractDeck) : HomeEvent()
    object CancelCardSelectionActionSnackbarButtonClicked : HomeEvent()

    // ChooseDeckListDialog
    class DeckListForAddingDecksWasSelected(val deckListId: Long) : HomeEvent()
    object CreateDeckListForAddingDecksButtonClicked : HomeEvent()
    class DeckListForRemovingDecksWasSelected(val deckListId: Long) : HomeEvent()

    // NoExerciseCardDialog
    object GoToDeckSettingsButtonClicked : HomeEvent()

    object FragmentResumed : HomeEvent()
}