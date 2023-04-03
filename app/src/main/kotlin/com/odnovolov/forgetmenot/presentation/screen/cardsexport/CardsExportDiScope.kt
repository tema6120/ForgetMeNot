package com.odnovolov.forgetmenot.presentation.screen.cardsexport

import com.odnovolov.forgetmenot.domain.interactor.operationsondecks.DeckExporter
import com.odnovolov.forgetmenot.persistence.shortterm.ExportDialogStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class CardsExportDiScope private constructor(
    initialDialogState: CardsExportDialogState? = null
) {
    private val dialogStateProvider = ExportDialogStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState,
        AppDiScope.get().cardsImportStorage
    )

    private val dialogState: CardsExportDialogState =
        initialDialogState ?: dialogStateProvider.load()

    val controller = CardsExportController(
        DeckExporter(),
        dialogState,
        AppDiScope.get().globalState,
        AppDiScope.get().longTermStateSaver,
        dialogStateProvider
    )

    val viewModel = CardsExportViewModel(
        dialogState,
        AppDiScope.get().cardsImportStorage
    )

    companion object : DiScopeManager<CardsExportDiScope>() {
        fun create(dialogState: CardsExportDialogState) = CardsExportDiScope(dialogState)

        override fun recreateDiScope() = CardsExportDiScope()

        override fun onCloseDiScope(diScope: CardsExportDiScope) {
            diScope.controller.dispose()
        }
    }
}