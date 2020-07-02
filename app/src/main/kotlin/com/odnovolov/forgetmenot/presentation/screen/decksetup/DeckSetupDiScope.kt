package com.odnovolov.forgetmenot.presentation.screen.decksetup

import com.odnovolov.forgetmenot.domain.interactor.deckeditor.DeckEditor
import com.odnovolov.forgetmenot.persistence.shortterm.DeckEditorStateProvider
import com.odnovolov.forgetmenot.persistence.shortterm.DeckSetupScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class DeckSetupDiScope private constructor(
    initialScreenState: DeckSetupScreenState? = null,
    initialDeckEditorState: DeckEditor.State? = null
) {
    private val screenStateProvider = DeckSetupScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val screenState: DeckSetupScreenState =
        initialScreenState ?: screenStateProvider.load()

    private val deckEditorStateProvider = DeckEditorStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState
    )

    private val deckEditorState: DeckEditor.State =
        initialDeckEditorState ?: deckEditorStateProvider.load()

    private val deckEditor = DeckEditor(
        deckEditorState,
        AppDiScope.get().globalState
    )

    val controller = DeckSetupController(
        deckEditor,
        screenState,
        AppDiScope.get().longTermStateSaver,
        deckEditorStateProvider,
        screenStateProvider
    )

    val viewModel = DeckSetupViewModel(
        screenState,
        deckEditorState,
        AppDiScope.get().globalState
    )

    companion object : DiScopeManager<DeckSetupDiScope>() {
        fun create(
            initialScreenState: DeckSetupScreenState,
            initialDeckEditorState: DeckEditor.State
        ) = DeckSetupDiScope(
            initialScreenState,
            initialDeckEditorState
        )

        override fun recreateDiScope() = DeckSetupDiScope()

        override fun onCloseDiScope(diScope: DeckSetupDiScope) {
            diScope.controller.dispose()
        }
    }
}