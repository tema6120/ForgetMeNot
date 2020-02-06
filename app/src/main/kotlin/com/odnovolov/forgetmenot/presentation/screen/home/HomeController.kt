package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.presentation.common.Store
import kotlinx.coroutines.flow.Flow

class HomeController(
    private val homeScreenState: HomeScreenState,
    private val deckReviewPreference: DeckReviewPreference,
    private val store: Store
) {
    private val commandFlow = EventFlow<HomeCommand>()
    val commands: Flow<HomeCommand> = commandFlow.get()

    fun onSearchTextChanged(searchText: String) {
        homeScreenState.searchText = searchText
    }

    fun onDisplayOnlyWithTasksCheckboxClicked() {

    }

    fun onDeckButtonClicked(deckId: Long) {

    }

    fun onDeckButtonLongClicked(deckId: Long) {

    }

    fun onWalkingModeMenuItemClicked(deckId: Long) {

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

    }

    fun onRemoveDecksMenuItemClicked() {

    }

    fun onStartExerciseInWalkingModeMenuItemClicked() {

    }

    fun onActionModeFinished() {

    }
}