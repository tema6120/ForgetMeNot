package com.odnovolov.forgetmenot.presentation.screen.cardfilterforexercise.cardlimit

import com.odnovolov.forgetmenot.persistence.shortterm.CardLimitDialogStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class CardLimitDiScope private constructor(
    initialDialogState: CardLimitDialogState? = null
) {
    private val dialogStateProvider = CardLimitDialogStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val dialogState: CardLimitDialogState =
        initialDialogState ?: dialogStateProvider.load()

    val controller = CardLimitController(
        AppDiScope.get().globalState.cardFilterForExercise,
        dialogState,
        dialogStateProvider,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = CardLimitViewModel(
        dialogState
    )

    companion object : DiScopeManager<CardLimitDiScope>() {
        fun create(dialogState: CardLimitDialogState) = CardLimitDiScope(dialogState)

        override fun recreateDiScope() = CardLimitDiScope()

        override fun onCloseDiScope(diScope: CardLimitDiScope) {
            diScope.controller.dispose()
        }
    }
}