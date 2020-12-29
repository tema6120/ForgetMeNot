package com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval

import com.odnovolov.forgetmenot.persistence.shortterm.ModifyIntervalDialogStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsDiScope

class ModifyIntervalDiScope private constructor(
    initialModifyIntervalDialogState: ModifyIntervalDialogState? = null
) {
    private val modifyIntervalDialogStateProvider = ModifyIntervalDialogStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val dialogState: ModifyIntervalDialogState =
        initialModifyIntervalDialogState ?: modifyIntervalDialogStateProvider.load()

    val controller = ModifyIntervalController(
        IntervalsDiScope.get()!!.intervalsSettings,
        dialogState,
        AppDiScope.get().longTermStateSaver,
        modifyIntervalDialogStateProvider
    )

    val viewModel = ModifyIntervalViewModel(
        dialogState
    )

    companion object : DiScopeManager<ModifyIntervalDiScope>() {
        fun create(initialModifyIntervalDialogState: ModifyIntervalDialogState) =
            ModifyIntervalDiScope(initialModifyIntervalDialogState)

        override fun recreateDiScope() = ModifyIntervalDiScope()

        override fun onCloseDiScope(diScope: ModifyIntervalDiScope) {
            diScope.controller.dispose()
        }
    }
}