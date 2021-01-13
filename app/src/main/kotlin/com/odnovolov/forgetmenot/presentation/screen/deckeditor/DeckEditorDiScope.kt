package com.odnovolov.forgetmenot.presentation.screen.deckeditor

import com.odnovolov.forgetmenot.persistence.shortterm.DeckEditorScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class DeckEditorDiScope private constructor(
    initialScreenState: DeckEditorScreenState? = null
) {
    private val screenStateProvider = DeckEditorScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState
    )

    val screenState: DeckEditorScreenState =
        initialScreenState ?: screenStateProvider.load()

    val controller = DeckEditorController(
        screenState,
        AppDiScope.get().navigator
    )

    val deckEditorViewModel = DeckEditorViewModel(
        screenState
    )

    companion object : DiScopeManager<DeckEditorDiScope>() {
        fun create(initialScreenState: DeckEditorScreenState) =
            DeckEditorDiScope(initialScreenState)

        override fun recreateDiScope() = DeckEditorDiScope()

        override fun onCloseDiScope(diScope: DeckEditorDiScope) {
            diScope.controller.dispose()
        }
    }
}