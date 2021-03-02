package com.odnovolov.forgetmenot.presentation.screen.deckeditor

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.BatchCardEditor
import com.odnovolov.forgetmenot.persistence.shortterm.DeckEditorScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.cardselectiontoolbar.CardSelectionController
import com.odnovolov.forgetmenot.presentation.screen.cardselectiontoolbar.CardSelectionViewModel

class DeckEditorDiScope private constructor(
    initialScreenState: DeckEditorScreenState? = null
) {
    val screenStateProvider = DeckEditorScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState
    )

    val screenState: DeckEditorScreenState =
        initialScreenState?.also { screenStateProvider.save(it) }
            ?: screenStateProvider.load()

    val batchCardEditor = BatchCardEditor(BatchCardEditor.State())

    val controller = DeckEditorController(
        batchCardEditor,
        screenState,
        AppDiScope.get().navigator,
        AppDiScope.get().globalState
    )

    val viewModel = DeckEditorViewModel(
        screenState,
        batchCardEditor.state
    )

    val cardSelectionController get() = CardSelectionController(
        batchCardEditor,
        AppDiScope.get().longTermStateSaver
    )

    val cardSelectionViewModel get() = CardSelectionViewModel(
        batchCardEditor.state
    )

    companion object : DiScopeManager<DeckEditorDiScope>() {
        fun create(initialScreenState: DeckEditorScreenState) =
            DeckEditorDiScope(initialScreenState)

        override fun recreateDiScope() = DeckEditorDiScope()

        override fun onCloseDiScope(diScope: DeckEditorDiScope) {
            diScope.controller.dispose()
            diScope.cardSelectionController.dispose()
        }
    }
}