package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.common.firstBlocking
import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.interactor.removedeck.RemoveDeckInteractor
import com.odnovolov.forgetmenot.domain.interactor.removedeck.RemoveDeckInteractor.Event.DecksHasRemoved
import com.odnovolov.forgetmenot.presentation.common.Store
import com.odnovolov.forgetmenot.presentation.screen.home.HomeCommand.ShowDeckRemovingMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

class HomeController(
    private val homeScreenState: HomeScreenState,
    private val deckReviewPreference: DeckReviewPreference,
    private val displayedDeckIds: Flow<List<Long>>,
    private val removeDeckInteractor: RemoveDeckInteractor,
    private val store: Store
) {
    private val commandFlow = EventFlow<HomeCommand>()
    val commands: Flow<HomeCommand> = merge(
        commandFlow.get(),
        removeDeckInteractor.events.toCommands()
    )

    private fun Flow<RemoveDeckInteractor.Event>.toCommands(): Flow<HomeCommand> {
        return this.map { event: RemoveDeckInteractor.Event ->
            when (event) {
                is DecksHasRemoved -> ShowDeckRemovingMessage(numberOfDecksRemoved = event.count)
            }
        }
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

    private fun startExercise(deckIds: List<Long>, isWalkingMode: Boolean) {
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