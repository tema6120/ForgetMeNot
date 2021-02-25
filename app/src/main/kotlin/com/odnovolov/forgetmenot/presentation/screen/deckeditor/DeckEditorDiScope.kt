package com.odnovolov.forgetmenot.presentation.screen.deckeditor

import com.odnovolov.forgetmenot.persistence.shortterm.DeckEditorScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class DeckEditorDiScope private constructor(
    initialScreenState: DeckEditorScreenState? = null
) {
    val screenStateProvider = DeckEditorScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState
    )

    val screenState: DeckEditorScreenState =
        initialScreenState?.also { screenStateProvider.save(it) }
            ?: screenStateProvider.load()

    val controller = DeckEditorController(
        screenState,
        AppDiScope.get().navigator
    )

    val viewModel = DeckEditorViewModel(
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