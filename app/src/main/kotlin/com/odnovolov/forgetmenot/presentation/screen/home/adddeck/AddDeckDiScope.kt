package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import com.odnovolov.forgetmenot.domain.interactor.deckadder.DeckAdder
import com.odnovolov.forgetmenot.persistence.shortterm.AddDeckScreenStateProvider
import com.odnovolov.forgetmenot.persistence.shortterm.AddDeckStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class AddDeckDiScope private constructor(
    initialDeckAdderState: DeckAdder.State? = null,
    initialAddDeckScreenState: AddDeckScreenState? = null
) {
    private val addDeckStateProvider = AddDeckStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val deckAdderState: DeckAdder.State =
        initialDeckAdderState ?: addDeckStateProvider.load()

    private val addDeckScreenStateProvider = AddDeckScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val addDeckScreenState: AddDeckScreenState =
        initialAddDeckScreenState ?: addDeckScreenStateProvider.load()

    private val deckAdder = DeckAdder(
        deckAdderState,
        AppDiScope.get().globalState
    )

    val controller = AddDeckController(
        addDeckScreenState,
        deckAdder,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        addDeckStateProvider,
        addDeckScreenStateProvider
    )

    val viewModel = AddDeckViewModel(
        deckAdderState,
        addDeckScreenState,
        AppDiScope.get().globalState
    )

    companion object : DiScopeManager<AddDeckDiScope>() {
        fun create(
            initialDeckAdderState: DeckAdder.State,
            initialAddDeckScreenState: AddDeckScreenState
        ) = AddDeckDiScope(
            initialDeckAdderState,
            initialAddDeckScreenState
        )

        override fun recreateDiScope() = AddDeckDiScope()

        override fun onCloseDiScope(diScope: AddDeckDiScope) {
            diScope.controller.dispose()
        }
    }
}