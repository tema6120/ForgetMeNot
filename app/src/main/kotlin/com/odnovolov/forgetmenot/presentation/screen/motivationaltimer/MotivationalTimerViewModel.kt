package com.odnovolov.forgetmenot.presentation.screen.motivationaltimer

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.ExercisePreference
import com.odnovolov.forgetmenot.domain.entity.NOT_TO_USE_TIMER
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

class MotivationalTimerViewModel(
    private val deckSettingsState: DeckSettings.State,
    private val screenState: MotivationalTimerScreenState
) {
    val isTimerEnabled: Flow<Boolean> =
        screenState.flowOf(MotivationalTimerScreenState::isTimerEnabled)

    val timeInput: String get() = screenState.timeInput

    private val realTimeForAnswer: Flow<Int?> = deckSettingsState.deck
        .flowOf(Deck::exercisePreference)
        .flatMapLatest { exercisePreference: ExercisePreference ->
            exercisePreference.flowOf(ExercisePreference::timeForAnswer)
        }

    val isOkButtonVisible: Flow<Boolean> = combine(
        realTimeForAnswer,
        isTimerEnabled,
        screenState.flowOf(MotivationalTimerScreenState::timeInput)
    ) { realTimeForAnswer: Int?, editedIsTimerEnabled: Boolean, timeInput: String ->
        val isUserInputValid = timeInput.toIntOrNull() ?: -1 > 0
        if (editedIsTimerEnabled && !isUserInputValid) {
            false
        } else {
            val editedTimeForAnswer: Int = when {
                !editedIsTimerEnabled -> NOT_TO_USE_TIMER
                else -> timeInput.toIntOrNull()!!
            }
            realTimeForAnswer != editedTimeForAnswer
        }
    }

    val currentTimeForAnswer: Int
        get() = deckSettingsState.deck.exercisePreference.timeForAnswer

    val editedTimeForAnswer: Int?
        get() = when {
            !screenState.isTimerEnabled -> NOT_TO_USE_TIMER
            else -> screenState.timeInput.toIntOrNull()
        }
}