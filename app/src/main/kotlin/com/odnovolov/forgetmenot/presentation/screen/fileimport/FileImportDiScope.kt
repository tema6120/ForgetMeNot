package com.odnovolov.forgetmenot.presentation.screen.fileimport

import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter
import com.odnovolov.forgetmenot.persistence.shortterm.FileImportScreenStateProvider
import com.odnovolov.forgetmenot.persistence.shortterm.FileImporterStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.CardsFileController
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.CardsFileViewModel
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.cards.ImportedCardsController
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.cards.ImportedCardsViewModel
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.sourcetext.ImportedTextEditorController
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.sourcetext.ImportedTextEditorViewModel
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.sourcetext.SyntaxHighlighting
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.sourcetext.fileformat.FileFormatController
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.sourcetext.fileformat.FileFormatViewModel

class FileImportDiScope private constructor(
    initialScreenState: FileImportScreenState? = null,
    initialFileImporterState: FileImporter.State? = null
) {
    private val screenStateProvider = FileImportScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val screenState: FileImportScreenState =
        initialScreenState ?: screenStateProvider.load()

    private val fileImporterStateProvider = FileImporterStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState,
        AppDiScope.get().fileImportStorage
    )

    private val fileImporterState: FileImporter.State =
        initialFileImporterState ?: fileImporterStateProvider.load()

    val fileImporter = FileImporter(
        fileImporterState,
        AppDiScope.get().globalState,
        AppDiScope.get().fileImportStorage
    )

    val fileImportController = FileImportController(
        fileImporter,
        AppDiScope.get().navigator,
        AppDiScope.get().globalState,
        AppDiScope.get().longTermStateSaver,
        fileImporterStateProvider
    )

    val fileImportViewModel = FileImportViewModel(
        fileImporterState
    )

    val cardsFileController = CardsFileController(
        fileImporter,
        AppDiScope.get().navigator,
        screenState,
        AppDiScope.get().longTermStateSaver,
        fileImporterStateProvider,
        screenStateProvider
    )

    fun cardsFileViewModel(id: Long) = CardsFileViewModel(
        id,
        fileImporterState,
        AppDiScope.get().globalState
    )

    val importedCardsController = ImportedCardsController(
        fileImporter,
        AppDiScope.get().longTermStateSaver,
        fileImporterStateProvider
    )

    fun importedCardsViewModel(id: Long) = ImportedCardsViewModel(
        id,
        fileImporterState
    )

    val importedTextEditorController = ImportedTextEditorController(
        fileImporter,
        AppDiScope.get().longTermStateSaver,
        fileImporterStateProvider
    )

    fun importedTextEditorViewModel(id: Long) = ImportedTextEditorViewModel(
        id,
        fileImporterState
    )

    val syntaxHighlighting = SyntaxHighlighting(
        fileImporter
    )

    val fileFormatController = FileFormatController(
        fileImporter,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        fileImporterStateProvider
    )

    fun fileFormatViewModel(id: Long) = FileFormatViewModel(
        id,
        fileImporterState,
        AppDiScope.get().fileImportStorage
    )

    companion object : DiScopeManager<FileImportDiScope>() {
        fun create(
            screenState: FileImportScreenState,
            fileImporterState: FileImporter.State
        ) = FileImportDiScope(
            screenState,
            fileImporterState
        )

        override fun recreateDiScope() = FileImportDiScope()

        override fun onCloseDiScope(diScope: FileImportDiScope) {
            with(diScope) {
                fileImportController.dispose()
                cardsFileController.dispose()
                importedCardsController.dispose()
                importedTextEditorController.dispose()
                syntaxHighlighting.dispose()
                fileFormatController.dispose()
            }
        }
    }
}