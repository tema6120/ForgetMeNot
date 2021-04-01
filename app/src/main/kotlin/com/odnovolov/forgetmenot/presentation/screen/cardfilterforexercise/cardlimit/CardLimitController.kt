package com.odnovolov.forgetmenot.presentation.screen.cardfilterforexercise.cardlimit

import com.odnovolov.forgetmenot.domain.interactor.exercise.CardFilterForExercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.CardFilterForExercise.Companion.CARD_FILTER_NO_LIMIT
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardfilterforexercise.cardlimit.CardLimitEvent.*

class CardLimitController(
    private val cardFilter: CardFilterForExercise,
    private val dialogState: CardLimitDialogState,
    private val dialogStateProvider: ShortTermStateProvider<CardLimitDialogState>,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<CardLimitEvent, Nothing>() {
    override fun handle(event: CardLimitEvent) {
        when (event) {
            LimitRadioButtonClicked -> {
                dialogState.isNoLimit = false
            }

            is DialogTextChanged -> {
                dialogState.dialogText = event.text
            }

            NoLimitRadioButtonClicked -> {
                dialogState.isNoLimit = true
            }

            OkButtonClicked -> {
                if (dialogState.isNoLimit) {
                    cardFilter.limit = CARD_FILTER_NO_LIMIT
                } else {
                    val maxNumberOfCards: Int? = dialogState.dialogText.toIntOrNull()
                    if (maxNumberOfCards != null && maxNumberOfCards > 0) {
                        cardFilter.limit = maxNumberOfCards
                    }
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        dialogStateProvider.save(dialogState)
    }
}