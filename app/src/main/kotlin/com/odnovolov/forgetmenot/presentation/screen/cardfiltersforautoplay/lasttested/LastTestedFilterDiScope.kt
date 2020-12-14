package com.odnovolov.forgetmenot.presentation.screen.cardfiltersforautoplay.lasttested

import com.odnovolov.forgetmenot.persistence.shortterm.LastTestedFilterDialogStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class LastTestedFilterDiScope private constructor(
    initialDialogState: LastTestedFilterDialogState? = null
) {
    private val dialogStateProvider = LastTestedFilterDialogStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val dialogState: LastTestedFilterDialogState =
        initialDialogState ?: dialogStateProvider.load()

    val controller = LastTestedFilterController(
        AppDiScope.get().globalState.cardFiltersForAutoplay,
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