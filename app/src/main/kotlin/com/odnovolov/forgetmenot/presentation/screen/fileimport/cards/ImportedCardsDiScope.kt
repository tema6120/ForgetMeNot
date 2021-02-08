package com.odnovolov.forgetmenot.presentation.screen.fileimport.cards

import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportDiScope

class ImportedCardsDiScope {
    val controller = ImportedCardsController(
        FileImportDiScope.getOrRecreate().fileImporter,
        AppDiScope.get().longTermStateSaver,
        FileImportDiScope.getOrRecreate().fileImporterStateProvider
    )

    val viewModel = ImportedCardsViewModel(
        FileImportDiScope.getOrRecreate().fileImporter.state
    )

    companion object : DiScopeManager<ImportedCardsDiScope>() {
        override fun recreateDiScope() = ImportedCardsDiScope()

        override fun onCloseDiScope(diScope: ImportedCardsDiScope) {
            diScope.controller.dispose()
        }
    }
}