package com.odnovolov.forgetmenot.presentation.screen.exercisesettings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseSettingsViewModel(
    exerciseSettings: ExerciseSettings,
    private val dialogState: CardsThresholdDialogState
) {
    val cardPrefilterMode: Flow<CardPrefilterMode> =
        exerciseSettings.flowOf(ExerciseSettings::cardPrefilterMode)

    val cardsThresholdDialogText: String
        get() = dialogState.text

    val cardsThresholdDialogPurpose: CardsThresholdDialogState.Purpose?
        get() = dialogState.purpose

    val isCardsThresholdDialogOkButtonEnabled: Flow<Boolean> =
        dialogState.flowOf(CardsThresholdDialogState::text)
            .map { input: String ->
                val numberOfCards = input.toIntOrNull() ?: 0
                numberOfCards > 0
            }

    val showProgressBar: Flow<Boolean> =
        exerciseSettings.flowOf(ExerciseSettings::showProgressBar)

    val showTextOfCardPosition: Flow<Boolean> =
        exerciseSettings.flowOf(ExerciseSettings::showTextOfCardPosition)

    val vibrateOnWrongAnswer: Flow<Boolean> =
        exerciseSettings.flowOf(ExerciseSettings::vibrateOnWrongAnswer)

    val goToNextCardAfterMarkingAsLearned: Flow<Boolean> =
        exerciseSettings.flowOf(ExerciseSettings::goToNextCardAfterMarkingAsLearned)

    val askToQuit: Flow<Boolean> =
        exerciseSettings.flowOf(ExerciseSettings::askToQuit)
}