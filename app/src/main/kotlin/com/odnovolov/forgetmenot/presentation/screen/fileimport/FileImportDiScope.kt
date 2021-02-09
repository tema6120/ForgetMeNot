package com.odnovolov.forgetmenot.presentation.screen.fileimport

import com.odnovolov.forgetmenot.domain.interactor.fileimport.*
import com.odnovolov.forgetmenot.persistence.shortterm.FileImporterStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class FileImportDiScope private constructor(
    initialFileImporterState: FileImporter.State? = null
) {
    val fileImporterStateProvider = FileImporterStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState
    )

    private val fileImporterState: FileImporter.State =
        initialFileImporterState ?: fileImporterStateProvider.load()

    val fileImporter = FileImporter(
        fileImporterState,
        AppDiScope.get().globalState
    )

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
        fun create(fileImporterState: FileImporter.State) = FileImportDiScope(fileImporterState)

        override fun recreateDiScope() = FileImportDiScope()

        override fun onCloseDiScope(diScope: FileImportDiScope) {
            with(diScope) {
                controller.dispose()
            }
        }
    }
}