package com.odnovolov.forgetmenot.presentation.screen.lasttested

import com.odnovolov.forgetmenot.domain.entity.CardFilterLastTested
import com.odnovolov.forgetmenot.persistence.shortterm.LastTestedFilterDialogStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.lasttested.LastTestedFilterDialogCaller.CardFilterForAutoplay
import com.odnovolov.forgetmenot.presentation.screen.lasttested.LastTestedFilterDialogCaller.CardFilterForExercise

class LastTestedFilterDiScope private constructor(
    initialDialogState: LastTestedFilterDialogState? = null
) {
    private val dialogStateProvider = LastTestedFilterDialogStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val dialogState: LastTestedFilterDialogState =
        initialDialogState ?: dialogStateProvider.load()

    private val cardFilter: CardFilterLastTested
        get() = when (dialogState.caller) {
            CardFilterForAutoplay -> AppDiScope.get().globalState.cardFilterForAutoplay
            CardFilterForExercise -> AppDiScope.get().globalState.cardFilterForExercise
        }

    val controller = LastTestedFilterController(
        cardFilter,
        dialogState,
        AppDiScope.get().longTermStateSaver,
        dialogStateProvider
    )

    val viewModel = LastTestedFilterViewModel(
        dialogState
    )

    companion object : DiScopeManager<LastTestedFilterDiScope>() {
        fun create(dialogState: LastTestedFilterDialogState) = LastTestedFilterDiScope(dialogState)

        override fun recreateDiScope() = LastTestedFilterDiScope()

        override fun onCloseDiScope(diScope: LastTestedFilterDiScope) {
            diScope.controller.dispose()
        }
    }
}