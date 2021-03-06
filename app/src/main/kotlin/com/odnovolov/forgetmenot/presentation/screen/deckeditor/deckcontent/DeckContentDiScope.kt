package com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent

import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorDiScope

class DeckContentDiScope {
    val controller = DeckContentController(
        DeckEditorDiScope.getOrRecreate().batchCardEditor,
        DeckEditorDiScope.getOrRecreate().screenState,
        AppDiScope.get().globalState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        DeckEditorDiScope.getOrRecreate().screenStateProvider,
        DeckEditorDiScope.getOrRecreate().batchCardEditorProvider
    )

    val viewModel = DeckContentViewModel(
        DeckEditorDiScope.getOrRecreate().screenState,
        DeckEditorDiScope.getOrRecreate().batchCardEditor.state
    )

    companion object : DiScopeManager<DeckContentDiScope>() {
        override fun recreateDiScope() = DeckContentDiScope()

        override fun onCloseDiScope(diScope: DeckContentDiScope) {
            diScope.controller.dispose()
        }
    }
}