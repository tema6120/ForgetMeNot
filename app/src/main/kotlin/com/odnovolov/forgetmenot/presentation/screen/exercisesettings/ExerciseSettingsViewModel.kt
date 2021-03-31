package com.odnovolov.forgetmenot.presentation.screen.exercisesettings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseSettingsViewModel(
    exerciseSettings: ExerciseSettings,
    private val screenState: ExerciseSettingsScreenState
) {
    val cardFilterDisplay: Flow<CardFilterDisplay> =
        exerciseSettings.flowOf(ExerciseSettings::cardFilterDisplay)

    val cardsThresholdForFilterDialogInput
        get() = screenState.cardsThresholdForFilterDialogInput

    val isCardsThresholdForShowingFilterDialogOkButtonEnabled: Flow<Boolean> =
        screenState.flowOf(ExerciseSettingsScreenState::cardsThresholdForFilterDialogInput)
            .map { input: String ->
                val numberOfCards = input.toIntOrNull() ?: 0
                numberOfCards > 0
            }
}