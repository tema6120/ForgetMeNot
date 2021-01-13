package com.odnovolov.forgetmenot.presentation.screen.deckeditor.renamedeck

import com.odnovolov.forgetmenot.domain.interactor.deckeditor.DeckEditor
import com.odnovolov.forgetmenot.persistence.shortterm.DeckEditorStateProvider
import com.odnovolov.forgetmenot.persistence.shortterm.RenameDeckDialogStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class RenameDeckDiScope private constructor(
    initialDeckEditorState: DeckEditor.State? = null,
    initialRenameDeckDialogState: RenameDeckDialogState? = null
) {
    private val deckEditorStateProvider = DeckEditorStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState
    )

    private val deckEditorState: DeckEditor.State =
        initialDeckEditorState ?: deckEditorStateProvider.load()

    private val dialogStateProvider = RenameDeckDialogStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val dialogState: RenameDeckDialogState =
        initialRenameDeckDialogState ?: dialogStateProvider.load()

    private val deckEditor = DeckEditor(
        deckEditorState,
        AppDiScope.get().globalState
    )

    val controller = RenameDeckController(
        deckEditor,
        dialogState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        deckEditorStateProvider,
        dialogStateProvider
    )

    val viewModel = RenameDeckViewModel(
        deckEditorState,
        dialogState,
        AppDiScope.get().globalState
    )

    companion object : DiScopeManager<RenameDeckDiScope>() {
        fun create(
            initialDeckEditorState: DeckEditor.State,
            initialRenameDeckDialogState: RenameDeckDialogState
        ) = RenameDeckDiScope(
            initialDeckEditorState,
            initialRenameDeckDialogState
        )

        override fun recreateDiScope(): RenameDeckDiScope = RenameDeckDiScope()

        override fun onCloseDiScope(diScope: RenameDeckDiScope) {
            diScope.controller.dispose()
        }
    }
}