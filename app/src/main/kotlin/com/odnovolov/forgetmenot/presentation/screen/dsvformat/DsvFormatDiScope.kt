package com.odnovolov.forgetmenot.presentation.screen.dsvformat

import com.odnovolov.forgetmenot.domain.interactor.cardsimport.DsvFormatEditor
import com.odnovolov.forgetmenot.persistence.shortterm.DsvFormatEditorStateProvider
import com.odnovolov.forgetmenot.persistence.shortterm.DsvFormatScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.CardsImportDiScope

class DsvFormatDiScope private constructor(
    initialDsvFormatEditorState: DsvFormatEditor.State? = null,
    initialScreenState: DsvFormatScreenState? = null
) {
    private val dsvFormatEditorStateProvider = DsvFormatEditorStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().cardsImportStorage
    )

    private val dsvFormatEditorState: DsvFormatEditor.State =
        initialDsvFormatEditorState ?: dsvFormatEditorStateProvider.load()

    private val screenStateProvider = DsvFormatScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val screenState: DsvFormatScreenState =
        initialScreenState ?: screenStateProvider.load()

    private val dsvFormatEditor = DsvFormatEditor(
        dsvFormatEditorState,
        AppDiScope.get().cardsImportStorage
    )

    val controller = DsvFormatController(
        dsvFormatEditor,
        CardsImportDiScope.getOrRecreate().cardsImporter,
        screenState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        dsvFormatEditorStateProvider,
        screenStateProvider
    )

    val viewModel = DsvFormatViewModel(
        dsvFormatEditorState,
        screenState,
        AppDiScope.get().cardsImportStorage
    )

    companion object : DiScopeManager<DsvFormatDiScope>() {
        fun create(
            dsvFormatEditorState: DsvFormatEditor.State,
            screenState: DsvFormatScreenState
        ) = DsvFormatDiScope(
            dsvFormatEditorState,
            screenState
        )

        override fun recreateDiScope() = DsvFormatDiScope()

        override fun onCloseDiScope(diScope: DsvFormatDiScope) {
            diScope.controller.dispose()
        }
    }
}