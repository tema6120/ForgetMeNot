package com.odnovolov.forgetmenot.presentation.screen.cardsimport

import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsImporter
import com.odnovolov.forgetmenot.persistence.shortterm.FileImportScreenStateProvider
import com.odnovolov.forgetmenot.persistence.shortterm.FileImporterStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.CardsFileController
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.CardsFileViewModel
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.cards.ImportedCardsController
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.cards.ImportedCardsViewModel
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.sourcetext.ImportedTextEditorController
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.sourcetext.ImportedTextEditorViewModel
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.sourcetext.SyntaxHighlighting
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.sourcetext.fileformat.FileFormatController
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.sourcetext.fileformat.FileFormatViewModel

class CardsImportDiScope private constructor(
    initialScreenState: CardsImportScreenState? = null,
    initialCardsImporterState: CardsImporter.State? = null
) {
    private val screenStateProvider = FileImportScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val screenState: CardsImportScreenState =
        initialScreenState ?: screenStateProvider.load()

    private val cardsImporterStateProvider = FileImporterStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState,
        AppDiScope.get().cardsImportStorage
    )

    private val cardsImporterState: CardsImporter.State =
        initialCardsImporterState ?: cardsImporterStateProvider.load()

    val cardsImporter = CardsImporter(
        cardsImporterState,
        AppDiScope.get().globalState,
        AppDiScope.get().cardsImportStorage
    )

    val cardsImportController = CardsImportController(
        cardsImporter,
        AppDiScope.get().navigator,
        AppDiScope.get().globalState,
        AppDiScope.get().longTermStateSaver,
        cardsImporterStateProvider
    )

    val cardsImportViewModel = CardsImportViewModel(
        cardsImporterState
    )

    val cardsFileController = CardsFileController(
        cardsImporter,
        AppDiScope.get().navigator,
        screenState,
        AppDiScope.get().longTermStateSaver,
        cardsImporterStateProvider,
        screenStateProvider
    )

    fun cardsFileViewModel(id: Long) = CardsFileViewModel(
        id,
        cardsImporterState,
        AppDiScope.get().globalState
    )

    val importedCardsController = ImportedCardsController(
        cardsImporter,
        AppDiScope.get().longTermStateSaver,
        cardsImporterStateProvider
    )

    fun importedCardsViewModel(id: Long) = ImportedCardsViewModel(
        id,
        cardsImporterState
    )

    val importedTextEditorController = ImportedTextEditorController(
        cardsImporter,
        AppDiScope.get().longTermStateSaver,
        cardsImporterStateProvider
    )

    fun importedTextEditorViewModel(id: Long) = ImportedTextEditorViewModel(
        id,
        cardsImporterState
    )

    val syntaxHighlighting = SyntaxHighlighting(
        cardsImporter
    )

    val fileFormatController = FileFormatController(
        cardsImporter,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        cardsImporterStateProvider
    )

    fun fileFormatViewModel(id: Long) = FileFormatViewModel(
        id,
        cardsImporterState,
        AppDiScope.get().cardsImportStorage
    )

    companion object : DiScopeManager<CardsImportDiScope>() {
        fun create(
            screenState: CardsImportScreenState,
            cardsImporterState: CardsImporter.State
        ) = CardsImportDiScope(
            screenState,
            cardsImporterState
        )

        override fun recreateDiScope() = CardsImportDiScope()

        override fun onCloseDiScope(diScope: CardsImportDiScope) {
            with(diScope) {
                cardsImportController.dispose()
                cardsFileController.dispose()
                importedCardsController.dispose()
                importedTextEditorController.dispose()
                syntaxHighlighting.dispose()
                fileFormatController.dispose()
            }
        }
    }
}