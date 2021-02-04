package com.odnovolov.forgetmenot.presentation.screen.renamedeck

import com.odnovolov.forgetmenot.persistence.shortterm.RenameDeckDialogStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class RenameDeckDiScope private constructor(
    initialRenameDeckDialogState: RenameDeckDialogState? = null
) {
    private val dialogStateProvider = RenameDeckDialogStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState
    )

    private val dialogState: RenameDeckDialogState =
        initialRenameDeckDialogState ?: dialogStateProvider.load()

    val controller = RenameDeckController(
        dialogState,
        AppDiScope.get().globalState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        dialogStateProvider
    )

    val viewModel = RenameDeckViewModel(
        dialogState,
        AppDiScope.get().globalState
    )

    companion object : DiScopeManager<RenameDeckDiScope>() {
        fun create(initialRenameDeckDialogState: RenameDeckDialogState) =
            RenameDeckDiScope(initialRenameDeckDialogState)

        override fun recreateDiScope(): RenameDeckDiScope = RenameDeckDiScope()

        override fun onCloseDiScope(diScope: RenameDeckDiScope) {
            diScope.controller.dispose()
        }
    }
}