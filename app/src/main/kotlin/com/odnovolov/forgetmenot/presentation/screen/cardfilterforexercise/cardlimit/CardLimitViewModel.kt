package com.odnovolov.forgetmenot.presentation.screen.cardfilterforexercise.cardlimit

import com.odnovolov.forgetmenot.domain.interactor.exercise.CardFilterForExercise.Companion.CARD_FILTER_NO_LIMIT
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transform

class CardLimitViewModel(
    private val dialogState: CardLimitDialogState
) {
    val isNoLimit: Flow<Boolean> = dialogState.flowOf(CardLimitDialogState::isNoLimit)

    val dialogText: String get() = dialogState.dialogText

    val maxNumberOfCards: Flow<Int> = dialogState.flowOf(CardLimitDialogState::dialogText)
        .transform { dialogText: String ->
            val maxNumberOfCards: Int? = dialogText.toIntOrNull()
            if (maxNumberOfCards != null && maxNumberOfCards != CARD_FILTER_NO_LIMIT) {
                emit(maxNumberOfCards)
            }
        }

    val isOkButtonEnabled: Flow<Boolean> = combine(
        isNoLimit,
        dialogState.flowOf(CardLimitDialogState::dialogText)
    ) { isNoLimit: Boolean, dialogText: String ->
        if (isNoLimit) {
            true
        } else {
            val maxNumberOfCards: Int? = dialogText.toIntOrNull()
            maxNumberOfCards != null && maxNumberOfCards > 0
        }
    }
}