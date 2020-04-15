package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.laps

import com.odnovolov.forgetmenot.persistence.shortterm.RepetitionLapsDialogStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.RepetitionSettingsDiScope

class RepetitionLapsDiScope private constructor(
    initialRepetitionLapsDialogState: RepetitionLapsDialogState? = null
) {
    private val dialogStateProvider = RepetitionLapsDialogStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val dialogState: RepetitionLapsDialogState =
        initialRepetitionLapsDialogState ?: dialogStateProvider.load()

    val controller = RepetitionLapsController(
        RepetitionSettingsDiScope.shareRepetitionSettings(),
        dialogState,
        dialogStateProvider,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = RepetitionLapsViewModel(
        dialogState
    )

    companion object : DiScopeManager<RepetitionLapsDiScope>() {
        fun create(dialogState: RepetitionLapsDialogState) = RepetitionLapsDiScope(dialogState)

        override fun recreateDiScope() = RepetitionLapsDiScope()

        override fun onCloseDiScope(diScope: RepetitionLapsDiScope) {
            diScope.controller.dispose()
        }
    }
}