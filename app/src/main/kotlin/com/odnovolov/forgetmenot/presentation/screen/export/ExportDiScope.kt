package com.odnovolov.forgetmenot.presentation.screen.export

import com.odnovolov.forgetmenot.domain.interactor.deckexporter.DeckExporter
import com.odnovolov.forgetmenot.persistence.shortterm.ExportDialogStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class ExportDiScope private constructor(
    initialDialogState: ExportDialogState? = null
) {
    private val dialogStateProvider = ExportDialogStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState,
        AppDiScope.get().fileImportStorage
    )

    private val dialogState: ExportDialogState =
        initialDialogState ?: dialogStateProvider.load()

    val controller = ExportController(
        DeckExporter(),
        dialogState,
        AppDiScope.get().globalState,
        AppDiScope.get().longTermStateSaver,
        dialogStateProvider
    )

    val viewModel = ExportViewModel(
        dialogState,
        AppDiScope.get().fileImportStorage
    )

    companion object : DiScopeManager<ExportDiScope>() {
        fun create(dialogState: ExportDialogState) = ExportDiScope(dialogState)

        override fun recreateDiScope() = ExportDiScope()

        override fun onCloseDiScope(diScope: ExportDiScope) {
            diScope.controller.dispose()
        }
    }
}