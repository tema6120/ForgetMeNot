package com.odnovolov.forgetmenot.presentation.screen.dsvformat

import com.odnovolov.forgetmenot.domain.interactor.fileimport.DsvFormatEditor
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportDiScope

class DsvFormatDiScope private constructor(
    initialDsvFormatEditorState: DsvFormatEditor.State? = null,
    initialScreenState: DsvFormatScreenState? = null
) {
    private val dsvFormatEditorState: DsvFormatEditor.State =
        initialDsvFormatEditorState ?: throw NullPointerException() // fixme

    private val screenState: DsvFormatScreenState =
        initialScreenState ?: throw NullPointerException() // fixme

    private val dsvFormatEditor = DsvFormatEditor(
        dsvFormatEditorState,
        FileImportDiScope.getOrRecreate().fileImportStorage
    )

    val controller = DsvFormatController(
        dsvFormatEditor,
        FileImportDiScope.getOrRecreate().fileImporter,
        screenState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = DsvFormatViewModel(
        dsvFormatEditorState,
        screenState,
        FileImportDiScope.getOrRecreate().fileImportStorage
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