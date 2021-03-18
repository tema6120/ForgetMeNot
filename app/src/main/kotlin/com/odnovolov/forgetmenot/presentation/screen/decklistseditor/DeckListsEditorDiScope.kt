package com.odnovolov.forgetmenot.presentation.screen.decklistseditor

import com.odnovolov.forgetmenot.domain.interactor.decklistseditor.DeckListsEditor
import com.odnovolov.forgetmenot.persistence.shortterm.DeckListEditorScreenStateProvider
import com.odnovolov.forgetmenot.persistence.shortterm.DeckListsEditorStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class DeckListsEditorDiScope private constructor(
    initialDeckListsEditorState: DeckListsEditor.State? = null,
    initialScreenState: DeckListEditorScreenState? = null
) {
    private val deckListsEditorStateProvider = DeckListsEditorStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState
    )

    private val deckListsEditorState: DeckListsEditor.State =
        initialDeckListsEditorState ?: deckListsEditorStateProvider.load()

    val screenStateProvider = DeckListEditorScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        deckListsEditorState.editingDeckLists
    )

    private val screenState: DeckListEditorScreenState =
        initialScreenState ?: screenStateProvider.load()

    private val deckListsEditor = DeckListsEditor(
        deckListsEditorState,
        AppDiScope.get().globalState
    )

    val controller = DeckListsEditorController(
        deckListsEditor,
        screenState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        deckListsEditorStateProvider,
        screenStateProvider
    )

    val viewModel = DeckListsEditorViewModel(
        deckListsEditorState,
        screenState
    )

    val colorChooserViewModel = ColorChooserViewModel(
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