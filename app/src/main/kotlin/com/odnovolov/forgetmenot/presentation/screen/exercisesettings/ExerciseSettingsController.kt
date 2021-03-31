package com.odnovolov.forgetmenot.presentation.screen.exercisesettings

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.CardFilterDisplay.WhenCardsMoreThan
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.ExerciseSettingsEvent.*

class ExerciseSettingsController(
    private val exerciseSettings: ExerciseSettings,
    private val screenState: ExerciseSettingsScreenState,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<ExerciseSettingsEvent, Nothing>() {
    override fun handle(event: ExerciseSettingsEvent) {
        when (event) {
            AlwaysShowCardFilterButtonClicked -> {
                exerciseSettings.cardFilterDisplay = CardFilterDisplay.Always
            }

            ConditionallyShowCardFilterButtonClicked -> {
                val cardsThresholdInput: Int =
                    screenState.cardsThresholdForFilterDialogInput.toIntOrNull() ?: 0
                exerciseSettings.cardFilterDisplay =
                    if (cardsThresholdInput > 0) {
                        WhenCardsMoreThan(cardsThresholdInput)
                    } else {
                        ExerciseSettings.DEFAULT_CARD_FILTER_DISPLAY
                    }
            }

            is CardsThresholdForFilterDialogInputTextChanged -> {
                screenState.cardsThresholdForFilterDialogInput = event.text
            }

            CardsThresholdForShowingFilterDialogOkButtonClicked -> {
                val cardsThresholdInput: Int =
                    screenState.cardsThresholdForFilterDialogInput.toIntOrNull() ?: 0
                if (cardsThresholdInput > 0) {
                    exerciseSettings.cardFilterDisplay = WhenCardsMoreThan(cardsThresholdInput)
                }
            }

            NeverShowCardFilterButtonClicked -> {
                exerciseSettings.cardFilterDisplay = CardFilterDisplay.Never
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}