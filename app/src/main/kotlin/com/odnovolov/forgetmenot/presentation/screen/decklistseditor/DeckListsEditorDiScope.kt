package com.odnovolov.forgetmenot.presentation.screen.decklistseditor

import com.odnovolov.forgetmenot.domain.interactor.decklistseditor.DeckListsEditor
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class DeckListsEditorDiScope private constructor(
    initialDeckListsEditorState: DeckListsEditor.State? = null,
    initialScreenState: DeckListEditorScreenState? = null
) {
    private val deckListsEditorState: DeckListsEditor.State =
        initialDeckListsEditorState ?: TODO()

    private val screenState: DeckListEditorScreenState =
        initialScreenState ?: TODO()

    private val deckListsEditor = DeckListsEditor(
        deckListsEditorState,
        AppDiScope.get().globalState
    )

    val controller = DeckListsEditorController(
        deckListsEditor,
        screenState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = DeckListsEditorViewModel(
        deckListsEditorState,
        screenState
    )

    companion object : DiScopeManager<DeckListsEditorDiScope>() {
        fun create(
            deckListsEditorState: DeckListsEditor.State,
            screenState: DeckListEditorScreenState
        ) = DeckListsEditorDiScope(
            deckListsEditorState,
            screenState
        )

        override fun recreateDiScope() = DeckListsEditorDiScope()

        override fun onCloseDiScope(diScope: DeckListsEditorDiScope) {
            diScope.controller.dispose()
        }
    }
}