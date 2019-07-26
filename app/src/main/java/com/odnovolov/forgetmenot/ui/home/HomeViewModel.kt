package com.odnovolov.forgetmenot.ui.home

import androidx.lifecycle.LiveData
import com.odnovolov.forgetmenot.common.ViewModel
import com.odnovolov.forgetmenot.ui.adddeck.AddDeckViewModel
import com.odnovolov.forgetmenot.ui.exercisecreator.ExerciseCreatorViewModel
import com.odnovolov.forgetmenot.ui.home.HomeViewModel.*

interface HomeViewModel : ViewModel<State, Action, Event> {

    val addDeckViewModel: AddDeckViewModel
    val exerciseCreatorViewModel: ExerciseCreatorViewModel

    data class State(
        val decksPreview: LiveData<List<DeckPreview>>,
        val deckSorting: LiveData<DeckSorting>
    )

    sealed class Action {
        data class NavigateToDeckSettings(val deckId: Int) : Action()
        object ShowDeckIsDeletedSnackbar : Action()
        object ShowDeckSortingBottomSheet : Action()
        object DismissDeckSortingBottomSheet : Action()
    }

    sealed class Event {
        object AddDeckButtonClicked : Event()
        data class DeckButtonClicked(val deckId: Int) : Event()
        data class SetupDeckMenuItemClicked(val deckId: Int) : Event()
        data class DeleteDeckMenuItemClicked(val deckId: Int) : Event()
        object DeckIsDeletedSnackbarCancelActionClicked : Event()
        data class SearchTextChanged(val searchText: String) : Event()
        object SortByMenuItemClicked : Event()
        object SortByNameTextViewClicked : Event()
        object SortByTimeCreatedTextViewClicked : Event()
        object SortByLastOpenedTextViewClicked : Event()
    }

}