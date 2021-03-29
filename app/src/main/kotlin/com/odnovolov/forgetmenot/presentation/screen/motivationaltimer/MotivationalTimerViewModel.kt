package com.odnovolov.forgetmenot.presentation.screen.motivationaltimer

import com.odnovolov.forgetmenot.domain.entity.NOT_TO_USE_TIMER
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.Tip
import kotlinx.coroutines.flow.Flow

class MotivationalTimerViewModel(
    private val deckSettingsState: DeckSettings.State,
    private val screenState: MotivationalTimerScreenState
) {
    val tip: Flow<Tip?> = screenState.flowOf(MotivationalTimerScreenState::tip)

    val isTimerEnabled: Flow<Boolean> =
        screenState.flowOf(MotivationalTimerScreenState::isTimerEnabled)

    val timeInput: String get() = screenState.timeInput

    val currentTimeForAnswer: Int
        get() = deckSettingsState.deck.exercisePreference.timeForAnswer

    val editedTimeForAnswer: Int?
        get() = when {
            !screenState.isTimerEnabled -> NOT_TO_USE_TIMER
            else -> screenState.timeInput.toIntOrNull()
        }
}