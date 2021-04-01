package com.odnovolov.forgetmenot.presentation.screen.exercisesettings

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.CardPrefilterMode.*
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.CardsThresholdDialogState.Purpose.ToChangeCardNumberThresholdForShowingFilter
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.CardsThresholdDialogState.Purpose.ToChangeCardNumberLimitation
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.ExerciseSettings.Companion.DEFAULT_CARD_NUMBER_LIMITATION
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.ExerciseSettingsEvent.*

class ExerciseSettingsController(
    private val exerciseSettings: ExerciseSettings,
    private val dialogState: CardsThresholdDialogState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val dialogStateProvider: ShortTermStateProvider<CardsThresholdDialogState>
) : BaseController<ExerciseSettingsEvent, Nothing>() {
    override fun handle(event: ExerciseSettingsEvent) {
        when (event) {
            DoNotFilterButtonClicked -> {
                exerciseSettings.cardPrefilterMode = DoNotFilter
            }

            LimitCardsButtonClicked -> {
                val cardPrefilterMode = exerciseSettings.cardPrefilterMode
                dialogState.text =
                    if (cardPrefilterMode is LimitCardsTo) {
                        cardPrefilterMode.numberOfCards.toString()
                    } else {
                        DEFAULT_CARD_NUMBER_LIMITATION.toString()
                    }
                dialogState.purpose = ToChangeCardNumberLimitation
                navigator.showCardsThresholdDialog()
            }

            ConditionallyShowCardFilterButtonClicked -> {
                val cardPrefilterMode = exerciseSettings.cardPrefilterMode
                dialogState.text =
                    if (cardPrefilterMode is ShowFilterWhenCardsMoreThan) {
                        cardPrefilterMode.numberOfCards.toString()
                    } else {
                        DEFAULT_CARD_NUMBER_LIMITATION.toString()
                    }
                dialogState.purpose = ToChangeCardNumberThresholdForShowingFilter
                navigator.showCardsThresholdDialog()
            }

            AlwaysShowCardFilterButtonClicked -> {
                exerciseSettings.cardPrefilterMode = AlwaysShowFilter
            }

            is CardsThresholdDialogInputTextChanged -> {
                dialogState.text = event.text
            }

            CardsThresholdDialogOkButtonClicked -> {
                val numberOfCards: Int = dialogState.text.toIntOrNull() ?: return
                if (numberOfCards < 1) return
                exerciseSettings.cardPrefilterMode = when (dialogState.purpose) {
                    ToChangeCardNumberLimitation -> {
                        LimitCardsTo(numberOfCards)
                    }
                    ToChangeCardNumberThresholdForShowingFilter -> {
                        ShowFilterWhenCardsMoreThan(numberOfCards)
                    }
                    null -> return
                }
            }

            ShowProgressBarButtonClicked -> {
                exerciseSettings.showProgressBar = !exerciseSettings.showProgressBar
            }

            ShowTextOfCardPositionButtonClicked -> {
                exerciseSettings.showTextOfCardPosition = !exerciseSettings.showTextOfCardPosition
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        dialogStateProvider.save(dialogState)
    }
}