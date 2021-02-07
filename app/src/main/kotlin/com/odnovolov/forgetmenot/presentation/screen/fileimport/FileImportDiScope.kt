package com.odnovolov.forgetmenot.presentation.screen.fileimport

import com.odnovolov.forgetmenot.domain.interactor.fileimport.*
import com.odnovolov.forgetmenot.persistence.shortterm.FileImporterStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class FileImportDiScope private constructor(
    importedFile: ImportedFile? = null
) {
    private val fileImportSettings = FileImportSettings(Charsets.UTF_8)

    private val fileImporterStateProvider = FileImporterStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState
    )

    val fileImporter: FileImporter =
        if (importedFile != null) {
            FileImporter(
                importedFile,
                AppDiScope.get().globalState,
                fileImportSettings
            )
        } else {
            val fileImporterState = fileImporterStateProvider.load()
            FileImporter(
                fileImporterState,
                AppDiScope.get().globalState,
                fileImportSettings
            )
        }

    val controller = FileImportController(
        fileImporter,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        fileImporterStateProvider
    )

    val viewModel = FileImportViewModel(
        fileImporter.state,
        AppDiScope.get().globalState
    )

    companion object : DiScopeManager<FileImportDiScope>() {
        fun create(importedFile: ImportedFile) = FileImportDiScope(importedFile)

        override fun recreateDiScope() = FileImportDiScope()

        override fun onCloseDiScope(diScope: FileImportDiScope) {
            with(diScope) {
                controller.dispose()
            }
        }
    }
}