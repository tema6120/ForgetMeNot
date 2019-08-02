package com.odnovolov.forgetmenot.ui.home

import androidx.lifecycle.LiveData
import com.odnovolov.forgetmenot.common.ViewModel
import com.odnovolov.forgetmenot.ui.home.HomeViewModel.*

interface HomeViewModel : ViewModel<State, Action, Event> {

    data class State(
        val decksPreview: LiveData<List<DeckPreview>>,
        val deckSorting: LiveData<DeckSorting>
    )

    sealed class Action {
        object SendAddDeckRequest : Action()
        object ShowDeckSortingBottomSheet : Action()
        object DismissDeckSortingBottomSheet : Action()
        data class SendCreateExerciseRequest(val deckId: Int) : Action()
        object NavigateToExercise : Action()
        data class NavigateToDeckSettings(val deckId: Int) : Action()
        object ShowDeckIsDeletedSnackbar : Action()
    }

    sealed class Event {
        object AddDeckMenuItemClicked : Event()
        object ExerciseWasCreated : Event()
        data class SearchTextChanged(val searchText: String) : Event()
        object SortByMenuItemClicked : Event()
        object SortByNameTextViewClicked : Event()
        object SortByTimeCreatedTextViewClicked : Event()
        object SortByLastOpenedTextViewClicked : Event()
        data class DeckButtonClicked(val deckId: Int) : Event()
        data class SetupDeckMenuItemClicked(val deckId: Int) : Event()
        data class DeleteDeckMenuItemClicked(val deckId: Int) : Event()
        object DeckIsDeletedSnackbarCancelActionClicked : Event()
    }

}