package com.odnovolov.forgetmenot.presentation.screen.fileimport.sourcetext

import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportDiScope

class ImportedTextEditorDiScope {
    val syntaxHighlighting = SyntaxHighlighting(
        FileImportDiScope.getOrRecreate().fileImporter
    )

    val controller = ImportedTextEditorController(
        FileImportDiScope.getOrRecreate().fileImporter,
        AppDiScope.get().longTermStateSaver,
        FileImportDiScope.getOrRecreate().fileImporterStateProvider
    )

    val viewModel = ImportedTextEditorViewModel(
        FileImportDiScope.getOrRecreate().fileImporter.state
    )

    companion object : DiScopeManager<ImportedTextEditorDiScope>() {
        override fun recreateDiScope() = ImportedTextEditorDiScope()

        override fun onCloseDiScope(diScope: ImportedTextEditorDiScope) {
            diScope.syntaxHighlighting.dispose()
            diScope.controller.dispose()
        }
    }
}