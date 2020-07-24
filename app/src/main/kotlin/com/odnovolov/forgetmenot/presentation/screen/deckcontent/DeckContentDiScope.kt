package com.odnovolov.forgetmenot.presentation.screen.deckcontent

import com.odnovolov.forgetmenot.domain.interactor.deckeditor.DeckEditor
import com.odnovolov.forgetmenot.domain.interactor.deckexporter.DeckExporter
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.decksetup.DeckSetupDiScope

class DeckContentDiScope {
    private val deckEditorState = DeckEditor.State(
        DeckSetupDiScope.shareDeck()
    )

    private val deckEditor = DeckEditor(
        deckEditorState,
        AppDiScope.get().globalState
    )

    val controller = DeckContentController(
        deckEditor,
        DeckExporter(),
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = DeckContentViewModel(
        deckEditorState
    )

    companion object : DiScopeManager<DeckContentDiScope>() {
        override fun recreateDiScope() = DeckContentDiScope()

        override fun onCloseDiScope(diScope: DeckContentDiScope) {
            diScope.controller.dispose()
        }
    }
}