package com.odnovolov.forgetmenot.presentation.screen.deckeditor

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.BatchCardEditor
import com.odnovolov.forgetmenot.persistence.shortterm.BatchCardEditorProvider
import com.odnovolov.forgetmenot.persistence.shortterm.DeckEditorScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class DeckEditorDiScope private constructor(
    initialScreenState: DeckEditorScreenState? = null,
    initialBatchCardEditor: BatchCardEditor? = null
) {
    val screenStateProvider = DeckEditorScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState
    )

    val screenState: DeckEditorScreenState =
        initialScreenState?.also(screenStateProvider::save)
            ?: screenStateProvider.load()

    val batchCardEditorProvider = BatchCardEditorProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState,
        key = "BatchCardEditor For DeckEditor"
    )

    val batchCardEditor: BatchCardEditor =
        initialBatchCardEditor ?: batchCardEditorProvider.load()

    val controller = DeckEditorController(
        batchCardEditor,
        screenState,
        AppDiScope.get().navigator,
        AppDiScope.get().globalState,
        AppDiScope.get().longTermStateSaver,
        batchCardEditorProvider
    )

    val viewModel = DeckEditorViewModel(
        screenState,
        batchCardEditor.state
    )

    companion object : DiScopeManager<DeckEditorDiScope>() {
        fun create(
            initialScreenState: DeckEditorScreenState,
            batchCardEditor: BatchCardEditor
        ) = DeckEditorDiScope(
            initialScreenState,
            batchCardEditor
        )

        override fun recreateDiScope() = DeckEditorDiScope()

        override fun onCloseDiScope(diScope: DeckEditorDiScope) {
            diScope.controller.dispose()
        }
    }
}