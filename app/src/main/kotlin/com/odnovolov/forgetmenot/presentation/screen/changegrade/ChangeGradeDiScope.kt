package com.odnovolov.forgetmenot.presentation.screen.changegrade

import com.odnovolov.forgetmenot.persistence.shortterm.ReadyToUseSerializableStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class ChangeGradeDiScope private constructor(
    initialDialogState: ChangeGradeDialogState? = null
) {
    private val dialogStateProvider = ReadyToUseSerializableStateProvider(
        ChangeGradeDialogState.serializer(),
        AppDiScope.get().json,
        AppDiScope.get().database,
        key = ChangeGradeDialogState::class.qualifiedName!!
    )

    private val dialogState: ChangeGradeDialogState =
        initialDialogState?.apply(dialogStateProvider::save) ?: dialogStateProvider.load()

    val controller = ChangeGradeController(
        dialogState,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = ChangeGradeViewModel(
        dialogState
    )

    companion object : DiScopeManager<ChangeGradeDiScope>() {
        fun create(dialogState: ChangeGradeDialogState) = ChangeGradeDiScope(dialogState)

        override fun recreateDiScope() = ChangeGradeDiScope()

        override fun onCloseDiScope(diScope: ChangeGradeDiScope) {
            diScope.controller.dispose()
        }
    }
}