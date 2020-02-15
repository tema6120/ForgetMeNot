package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.common.firstBlocking
import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.interactor.prepareexercise.PrepareExerciseInteractor
import com.odnovolov.forgetmenot.domain.interactor.prepareexercise.PrepareExerciseInteractor.Event.ExerciseIsReady
import com.odnovolov.forgetmenot.domain.interactor.prepareexercise.PrepareExerciseInteractor.Event.NoCardIsReadyForExercise
import com.odnovolov.forgetmenot.domain.interactor.removedeck.RemoveDeckInteractor
import com.odnovolov.forgetmenot.domain.interactor.removedeck.RemoveDeckInteractor.Event.DecksHasRemoved
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.Store
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseViewModel
import com.odnovolov.forgetmenot.presentation.screen.home.HomeCommand.ShowDeckRemovingMessage
import com.odnovolov.forgetmenot.presentation.screen.home.HomeCommand.ShowNoCardIsReadyForExerciseMessage
import com.soywiz.klock.DateTime
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import org.koin.core.KoinComponent

class HomeController(
    private val homeScreenState: HomeScreenState,
    private val deckReviewPreference: DeckReviewPreference,
    private val removeDeckInteractor: RemoveDeckInteractor,
    private val prepareExerciseInteractor: PrepareExerciseInteractor,
    private val navigator: Navigator,
    private val store: Store
) : KoinComponent {
    private val coroutineScope = MainScope()
    private val commandFlow = EventFlow<HomeCommand>()
    val commands: Flow<HomeCommand> = merge(
        commandFlow.get(),
        removeDeckInteractor.events.map { event: RemoveDeckInteractor.Event ->
            when (event) {
                is DecksHasRemoved -> ShowDeckRemovingMessage(numberOfDecksRemoved = event.count)
            }
        }
    )
    lateinit var displayedDeckIds: Flow<List<Long>>

    init {
        prepareExerciseInteractor.events
            .onEach { event: PrepareExerciseInteractor.Event ->
                when (event) {
                    is ExerciseIsReady -> {
                        event.exerciseState.exerciseCards.map { it.base.deck }
                            .distinct()
                            .forEach {
                                deck -> deck.lastOpenedAt = DateTime.now()
                            }
                        val koinScope = getKoin().createScope<ExerciseViewModel>()
                        koinScope.declare(event.exerciseState, override = true)
                        navigator.navigateToExercise()
                    }
                    NoCardIsReadyForExercise -> {
                        commandFlow.send(ShowNoCardIsReadyForExerciseMessage)
                    }
                }
            }
            .launchIn(coroutineScope)
    }

    fun onSearchTextChanged(searchText: String) {
        homeScreenState.searchText = searchText
    }

    fun onDisplayOnlyWithTasksCheckboxClicked() {
        with(deckReviewPreference) { displayOnlyWithTasks = !displayOnlyWithTasks }
        store.saveStateByRegistry()
    }

    fun onDeckButtonClicked(deckId: Long) {
        if (homeScreenState.selectedDeckIds.isNotEmpty()) {
            toggleDeckSelection(deckId)
        } else {
            startExercise(deckIds = listOf(deckId), isWalkingMode = false)
        }
    }

    fun onDeckButtonLongClicked(deckId: Long) {
        toggleDeckSelection(deckId)
    }

    fun onWalkingModeMenuItemClicked(deckId: Long) {
        startExercise(deckIds = listOf(deckId), isWalkingMode = true)
    }

    fun onRepetitionModeMenuItemClicked(deckId: Long) {

    }

    fun onSetupDeckMenuItemClicked(deckId: Long) {

    }

    fun onRemoveDeckMenuItemClicked(deckId: Long) {
        removeDeckInteractor.removeDeck(deckId)
        store.saveStateByRegistry()
    }

    fun onDecksRemovedSnackbarCancelActionClicked() {
        removeDeckInteractor.restoreDecks()
        store.saveStateByRegistry()
    }

    fun onStartExerciseMenuItemClicked() {

    }

    fun onSelectAllDecksMenuItemClicked() {
        homeScreenState.selectedDeckIds = displayedDeckIds.firstBlocking()
    }

    fun onRemoveDecksMenuItemClicked() {
        val deckIds = homeScreenState.selectedDeckIds
        removeDeckInteractor.removeDecks(deckIds)
        store.saveStateByRegistry()
        homeScreenState.selectedDeckIds = emptyList()
    }

    fun onStartExerciseInWalkingModeMenuItemClicked() {

    }

    fun onActionModeFinished() {
        if (homeScreenState.selectedDeckIds.isNotEmpty()) {
            homeScreenState.selectedDeckIds = emptyList()
        }
    }

    fun onViewModelCleared() {
        coroutineScope.cancel()
        store.save(homeScreenState)
    }

    private fun startExercise(deckIds: List<Long>, isWalkingMode: Boolean) {
        prepareExerciseInteractor.prepare(deckIds)
    }

    private fun toggleDeckSelection(deckId: Long) {
        with(homeScreenState) {
            if (deckId in selectedDeckIds) {
                selectedDeckIds -= deckId
            } else {
                selectedDeckIds += deckId
            }
        }
    }
}