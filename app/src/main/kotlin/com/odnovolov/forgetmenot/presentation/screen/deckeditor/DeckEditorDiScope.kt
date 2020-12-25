package com.odnovolov.forgetmenot.presentation.screen.deckeditor

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.DeckEditor
import com.odnovolov.forgetmenot.persistence.shortterm.DeckEditorStateProvider
import com.odnovolov.forgetmenot.persistence.shortterm.DeckEditorScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class DeckEditorDiScope private constructor(
    initialScreenState: DeckEditorScreenState? = null,
    initialDeckEditorState: DeckEditor.State? = null
) {
    private val screenStateProvider = DeckEditorScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState
    )

    val screenState: DeckEditorScreenState =
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

    val controller = DeckEditorController(
        deckEditor,
        screenState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        deckEditorStateProvider,
        screenStateProvider
    )

    val deckEditorViewModel = DeckEditorViewModel(
        screenState,
        deckEditorState,
        AppDiScope.get().globalState
    )

    companion object : DiScopeManager<DeckEditorDiScope>() {
        fun create(
            initialScreenState: DeckEditorScreenState,
            initialDeckEditorState: DeckEditor.State
        ) = DeckEditorDiScope(
            initialScreenState,
            initialDeckEditorState
        )

        fun shareDeck(): Deck {
            if (diScope == null) {
                diScope = recreateDiScope()
            }
            return diScope!!.screenState.relevantDeck
        }

        override fun recreateDiScope() = DeckEditorDiScope()

        override fun onCloseDiScope(diScope: DeckEditorDiScope) {
            diScope.controller.dispose()
        }
    }
}