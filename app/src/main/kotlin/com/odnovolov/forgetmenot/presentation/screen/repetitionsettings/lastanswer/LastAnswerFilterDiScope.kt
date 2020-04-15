package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.lastanswer

import com.odnovolov.forgetmenot.persistence.shortterm.LastAnswerFilterDialogStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.RepetitionSettingsDiScope

class LastAnswerFilterDiScope private constructor(
    initialDialogState: LastAnswerFilterDialogState? = null
) {
    private val dialogStateProvider = LastAnswerFilterDialogStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val dialogState: LastAnswerFilterDialogState =
        initialDialogState ?: dialogStateProvider.load()

    val controller = LastAnswerFilterController(
        RepetitionSettingsDiScope.shareRepetitionSettings(),
        dialogState,
        AppDiScope.get().longTermStateSaver,
        dialogStateProvider
    )

    val viewModel = LastAnswerFilterViewModel(
        dialogState
    )

    companion object : DiScopeManager<LastAnswerFilterDiScope>() {
        fun create(dialogState: LastAnswerFilterDialogState) = LastAnswerFilterDiScope(dialogState)

        override fun recreateDiScope() = LastAnswerFilterDiScope()

        override fun onCloseDiScope(diScope: LastAnswerFilterDiScope) {
            diScope.controller.dispose()
        }
    }
}