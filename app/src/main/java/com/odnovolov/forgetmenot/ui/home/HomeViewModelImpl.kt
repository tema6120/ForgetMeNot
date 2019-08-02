package com.odnovolov.forgetmenot.ui.home

import androidx.lifecycle.*
import com.odnovolov.forgetmenot.common.LiveEvent
import com.odnovolov.forgetmenot.entity.Deck
import com.odnovolov.forgetmenot.ui.home.DeckSorting.*
import com.odnovolov.forgetmenot.ui.home.HomeViewModel.*
import com.odnovolov.forgetmenot.ui.home.HomeViewModel.Action.*
import com.odnovolov.forgetmenot.ui.home.HomeViewModel.Event.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModelImpl(
    private val repository: HomeRepository
) : ViewModel(), HomeViewModel {

    class Factory(
        private val repository: HomeRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return HomeViewModelImpl(repository) as T
        }
    }

    private val decks: LiveData<List<Deck>> = repository.getDecks()
    private val searchText = MutableLiveData("")

    private val deckSorting: MutableLiveData<DeckSorting> = repository.getDeckSorting(initialValue = BY_TIME_CREATED)
    private val decksPreview = MediatorLiveData<List<DeckPreview>>().apply {
        fun updateValue() {
            var decks = decks.value ?: return
            val searchText = searchText.value!!
            val deckSorting = deckSorting.value ?: return

            viewModelScope.launch {
                val updatedValue = withContext(Default) {
                    if (searchText.isNotEmpty()) {
                        decks = decks.filter { it.name.contains(searchText) }
                    }
                    decks = when (deckSorting) {
                        BY_TIME_CREATED -> decks.sortedBy { it.createdAt }
                        BY_NAME -> decks.sortedBy { it.name }
                        BY_LAST_OPENED -> decks.sortedByDescending { it.lastOpenedAt }
                    }
                    decks.map { deck: Deck ->
                        val passedLaps: Int = deck.cards
                            .filter { card -> !card.isLearned }
                            .map { card -> card.lap }
                            .min() ?: 0
                        val progress = DeckPreview.Progress(
                            learned = deck.cards.filter { it.isLearned }.size,
                            total = deck.cards.size
                        )
                        DeckPreview(
                            deck.id,
                            deck.name,
                            passedLaps,
                            progress
                        )
                    }
                }
                value = updatedValue
            }
        }

        addSource(decks) { updateValue() }
        addSource(searchText) { updateValue() }
        addSource(deckSorting) { updateValue() }
    }

    override val state = State(
        decksPreview,
        deckSorting
    )

    private val actionSender = LiveEvent<Action>()
    override val action: LiveData<Action> = actionSender

    override fun onEvent(event: Event) {
        when (event) {
            AddDeckMenuItemClicked -> {
                actionSender.send(SendAddDeckRequest)
            }
            ExerciseWasCreated -> {
                actionSender.send(NavigateToExercise)
            }
            is SearchTextChanged -> {
                searchText.value = event.searchText
            }
            SortByMenuItemClicked -> {
                actionSender.send(ShowDeckSortingBottomSheet)
            }
            SortByNameTextViewClicked -> {
                deckSorting.value = BY_NAME
                actionSender.send(DismissDeckSortingBottomSheet)
            }
            SortByTimeCreatedTextViewClicked -> {
                deckSorting.value = BY_TIME_CREATED
                actionSender.send(DismissDeckSortingBottomSheet)
            }
            SortByLastOpenedTextViewClicked -> {
                deckSorting.value = BY_LAST_OPENED
                actionSender.send(DismissDeckSortingBottomSheet)
            }
            is DeckButtonClicked -> {
                actionSender.send(SendCreateExerciseRequest(event.deckId))
            }
            is SetupDeckMenuItemClicked -> {
                actionSender.send(NavigateToDeckSettings(event.deckId))
            }
            is DeleteDeckMenuItemClicked -> {
                viewModelScope.launch {
                    val numberOfDeletedDecks = withContext(IO) {
                        repository.deleteDeckCreatingBackup(event.deckId)
                    }
                    if (numberOfDeletedDecks == 1) {
                        actionSender.send(ShowDeckIsDeletedSnackbar)
                    }
                }
            }
            DeckIsDeletedSnackbarCancelActionClicked -> {
                viewModelScope.launch(IO) {
                    repository.restoreLastDeletedDeck()
                }
            }
        }
    }

}