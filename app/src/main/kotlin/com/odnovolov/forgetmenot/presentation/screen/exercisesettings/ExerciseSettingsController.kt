package com.odnovolov.forgetmenot.presentation.screen.exercisesettings

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.CardsThresholdDialogState.Purpose.ToChangeCardNumberThresholdForShowingFilter
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.CardsThresholdDialogState.Purpose.ToChangeCardNumberLimitation
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.ExerciseSettings.Companion.DEFAULT_CARD_NUMBER_LIMITATION
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.ExerciseSettingsEvent.*

class ExerciseSettingsController(
    private val exerciseSettings: ExerciseSettings,
    private val dialogState: CardsThresholdDialogState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<ExerciseSettingsEvent, Nothing>() {
    override fun handle(event: ExerciseSettingsEvent) {
        when (event) {
            NeverFilterCardsButtonClicked -> {
                exerciseSettings.cardPrefilterMode = CardPrefilterMode.Never
            }

            LimitCardsToButtonClicked -> {
                val cardPrefilterMode = exerciseSettings.cardPrefilterMode
                dialogState.text =
                    if (cardPrefilterMode is CardPrefilterMode.LimitCardsTo) {
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
                    if (cardPrefilterMode is CardPrefilterMode.ShowFilterWhenCardsMoreThan) {
                        cardPrefilterMode.numberOfCards.toString()
                    } else {
                        DEFAULT_CARD_NUMBER_LIMITATION.toString()
                    }
                dialogState.purpose = ToChangeCardNumberThresholdForShowingFilter
                navigator.showCardsThresholdDialog()
            }

            AlwaysShowCardFilterButtonClicked -> {
                exerciseSettings.cardPrefilterMode = CardPrefilterMode.AlwaysShowFilter
            }

            is CardsThresholdDialogInputTextChanged -> {
                dialogState.text = event.text
            }

            CardsThresholdDialogOkButtonClicked -> {
                val numberOfCards: Int = dialogState.text.toIntOrNull() ?: return
                if (numberOfCards < 1) return
                exerciseSettings.cardPrefilterMode = when (dialogState.purpose) {
                    ToChangeCardNumberLimitation -> {
                        CardPrefilterMode.LimitCardsTo(numberOfCards)
                    }
                    ToChangeCardNumberThresholdForShowingFilter -> {
                        CardPrefilterMode.ShowFilterWhenCardsMoreThan(numberOfCards)
                    }
                    null -> return
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}