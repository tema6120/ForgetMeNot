package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.common.firstBlocking
import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.presentation.common.Store
import kotlinx.coroutines.flow.Flow

class HomeController(
    private val homeScreenState: HomeScreenState,
    private val deckReviewPreference: DeckReviewPreference,
    private val displayedDeckIds: Flow<List<Long>>,
    private val store: Store
) {
    private val commandFlow = EventFlow<HomeCommand>()
    val commands: Flow<HomeCommand> = commandFlow.get()

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

    }

    fun onDecksRemovedSnackbarCancelActionClicked() {

    }

    fun onStartExerciseMenuItemClicked() {

    }

    fun onSelectAllDecksMenuItemClicked() {
        homeScreenState.selectedDeckIds = displayedDeckIds.firstBlocking()
    }

    fun onRemoveDecksMenuItemClicked() {

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
        with (homeScreenState) {
            if (deckId in selectedDeckIds) {
                selectedDeckIds -= deckId
            } else {
                selectedDeckIds += deckId
            }
        }
    }
}