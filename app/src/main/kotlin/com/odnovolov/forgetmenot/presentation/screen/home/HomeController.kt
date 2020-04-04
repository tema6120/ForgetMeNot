package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.deckremover.DeckRemover
import com.odnovolov.forgetmenot.domain.interactor.deckremover.DeckRemover.Event.DecksHasRemoved
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseStateCreator
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseStateCreator.NoCardIsReadyForExercise
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionStateCreator
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.firstBlocking
import com.odnovolov.forgetmenot.presentation.screen.home.HomeCommand.ShowDeckRemovingMessage
import com.odnovolov.forgetmenot.presentation.screen.home.HomeCommand.ShowNoCardIsReadyForExerciseMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

class HomeController(
    private val homeScreenState: HomeScreenState,
    private val deckReviewPreference: DeckReviewPreference,
    private val deckRemover: DeckRemover,
    private val exerciseStateCreator: ExerciseStateCreator,
    private val globalState: GlobalState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val homeScreenStateProvider: ShortTermStateProvider<HomeScreenState>
) {
    private val commandFlow = EventFlow<HomeCommand>()
    val commands: Flow<HomeCommand> = merge(
        commandFlow.get(),
        deckRemover.events.map { event: DeckRemover.Event ->
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
        longTermStateSaver.saveStateByRegistry()
    }

    fun onSettingsButtonClicked() {
        homeScreenState.selectedDeckIds = emptyList()
        navigator.navigateToSettings()
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
        startRepetitionSettings(listOf(deckId))
    }

    fun onRepetitionModeMultiSelectMenuItemClicked() {
        val deckIds: List<Long> = homeScreenState.selectedDeckIds
        homeScreenState.selectedDeckIds = emptyList()
        startRepetitionSettings(deckIds)
    }

    private fun startRepetitionSettings(deckIds: List<Long>) {
        val decks: List<Deck> = globalState.decks.filter { it.id in deckIds }
        val repetitionCreatorState = RepetitionStateCreator.State(decks)
        navigator.navigateToRepetitionSettings(repetitionCreatorState)
    }

    fun onSetupDeckMenuItemClicked(deckId: Long) {
        val deck: Deck = globalState.decks.find { it.id == deckId } ?: return
        val deckSettingsState = DeckSettings.State(deck)
        navigator.navigateToDeckSettings(deckSettingsState)
    }

    fun onRemoveDeckMenuItemClicked(deckId: Long) {
        deckRemover.removeDeck(deckId)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onDecksRemovedSnackbarCancelActionClicked() {
        deckRemover.restoreDecks()
        longTermStateSaver.saveStateByRegistry()
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
        deckRemover.removeDecks(deckIds)
        longTermStateSaver.saveStateByRegistry()
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
        val exerciseState: Exercise.State = try {
            exerciseStateCreator.create(deckIds, isWalkingMode)
        } catch (e: NoCardIsReadyForExercise) {
            commandFlow.send(ShowNoCardIsReadyForExerciseMessage)
            return
        }
        longTermStateSaver.saveStateByRegistry()
        navigator.navigateToExercise(exerciseState)
    }

    private fun toggleDeckSelection(deckId: Long) {
        with(homeScreenState) {
            selectedDeckIds = if (deckId in selectedDeckIds) {
                selectedDeckIds - deckId
            } else {
                selectedDeckIds + deckId
            }
        }
    }

    fun onFragmentPause() {
        homeScreenStateProvider.save(homeScreenState)
    }
}