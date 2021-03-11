package com.odnovolov.forgetmenot.presentation.screen.decklistseditor

import com.odnovolov.forgetmenot.domain.interactor.decklistseditor.DeckListsEditor
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class DeckListsEditorDiScope private constructor(
    initialDeckListsEditorState: DeckListsEditor.State? = null
) {
    private val deckListsEditorState: DeckListsEditor.State =
        initialDeckListsEditorState ?: TODO()

    private val deckListsEditor = DeckListsEditor(
        deckListsEditorState,
        AppDiScope.get().globalState
    )

    val controller = DeckListsEditorController(
        deckListsEditor,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = DeckListsEditorViewModel(
        deckListsEditorState
    )

    companion object : DiScopeManager<DeckListsEditorDiScope>() {
        fun create(
            deckListsEditorState: DeckListsEditor.State
        ) = DeckListsEditorDiScope(
            deckListsEditorState
        )

        override fun recreateDiScope() = DeckListsEditorDiScope()

        override fun onCloseDiScope(diScope: DeckListsEditorDiScope) {
            diScope.controller.dispose()
        }
    }
}