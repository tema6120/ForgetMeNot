package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseStateCreator
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseStateCreator.NoCardIsReadyForExercise
import com.odnovolov.forgetmenot.domain.interactor.removedeck.RemoveDeckInteractor
import com.odnovolov.forgetmenot.domain.interactor.removedeck.RemoveDeckInteractor.Event.DecksHasRemoved
import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionStateCreator
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.Store
import com.odnovolov.forgetmenot.presentation.common.firstBlocking
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DECK_SETTINGS_SCOPED_ID
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsScreenState
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsViewModel
import com.odnovolov.forgetmenot.presentation.screen.exercise.EXERCISE_SCOPE_ID
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseViewModel
import com.odnovolov.forgetmenot.presentation.screen.home.HomeCommand.ShowDeckRemovingMessage
import com.odnovolov.forgetmenot.presentation.screen.home.HomeCommand.ShowNoCardIsReadyForExerciseMessage
import com.odnovolov.forgetmenot.presentation.screen.repetition.REPETITION_SCOPE_ID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import org.koin.core.KoinComponent

class HomeController(
    private val homeScreenState: HomeScreenState,
    private val deckReviewPreference: DeckReviewPreference,
    private val removeDeckInteractor: RemoveDeckInteractor,
    private val exerciseStateCreator: ExerciseStateCreator,
    private val repetitionStateCreator: RepetitionStateCreator,
    private val globalState: GlobalState,
    private val navigator: Navigator,
    private val store: Store
) : KoinComponent {
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
        val repetitionState: Repetition.State = repetitionStateCreator.create(listOf(deckId))
        val koinScope = getKoin().createScope<Repetition>(REPETITION_SCOPE_ID)
        koinScope.declare(repetitionState, override = true)
        navigator.navigateToRepetition()
    }

    fun onSetupDeckMenuItemClicked(deckId: Long) {
        val deck: Deck = globalState.decks.find { it.id == deckId } ?: return
        val deckSettingsState = DeckSettings.State(deck)
        val koinScope = getKoin().createScope<DeckSettingsViewModel>(DECK_SETTINGS_SCOPED_ID)
        koinScope.declare(deckSettingsState, override = true)
        koinScope.declare(DeckSettingsScreenState(), override = true)
        navigator.navigateToDeckSettings()
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
        val deckIds: List<Long> = homeScreenState.selectedDeckIds
        homeScreenState.selectedDeckIds = emptyList()
        startExercise(deckIds, isWalkingMode = false)
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
        val deckIds: List<Long> = homeScreenState.selectedDeckIds
        homeScreenState.selectedDeckIds = emptyList()
        startExercise(deckIds, isWalkingMode = true)
    }

    fun onActionModeFinished() {
        if (homeScreenState.selectedDeckIds.isNotEmpty()) {
            homeScreenState.selectedDeckIds = emptyList()
        }
    }

    private fun startExercise(deckIds: List<Long>, isWalkingMode: Boolean) {
        try {
            val exerciseState: Exercise.State = exerciseStateCreator.create(deckIds, isWalkingMode)
            val koinScope = getKoin().createScope<ExerciseViewModel>(EXERCISE_SCOPE_ID)
            koinScope.declare(exerciseState, override = true)
            navigator.navigateToExercise()
        } catch (e: NoCardIsReadyForExercise) {
            commandFlow.send(ShowNoCardIsReadyForExerciseMessage)
        }
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

    fun onCleared() {
        store.save(homeScreenState)
    }
}