package com.odnovolov.forgetmenot.presentation.screen.fileimport

import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImportSettings
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter
import com.odnovolov.forgetmenot.domain.interactor.fileimport.ImportedFile
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class FileImportDiScope private constructor(
    importedFile: ImportedFile? = null
) {
    private val fileImportSettings = FileImportSettings(Charsets.UTF_8)

    val fileImporter =
        if (importedFile != null) {
            FileImporter(importedFile, AppDiScope.get().globalState, fileImportSettings)
        } else {
            // todo
            throw NullPointerException()
        }

    val controller = FileImportController(
        fileImporter,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = FileImportViewModel(
        fileImporter.state,
        AppDiScope.get().globalState
    )

    companion object : DiScopeManager<FileImportDiScope>() {
        fun create(importedFile: ImportedFile) =
            FileImportDiScope(importedFile)

        override fun recreateDiScope() = FileImportDiScope()

        override fun onCloseDiScope(diScope: FileImportDiScope) {
            with(diScope) {
                controller.dispose()
            }
        }
    }
}