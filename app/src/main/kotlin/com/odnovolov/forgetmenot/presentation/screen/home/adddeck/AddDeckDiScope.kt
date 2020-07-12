package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import com.odnovolov.forgetmenot.domain.interactor.deckcreator.DeckCreator
import com.odnovolov.forgetmenot.domain.interactor.deckcreator.DeckFromFileCreator
import com.odnovolov.forgetmenot.persistence.shortterm.AddDeckScreenStateProvider
import com.odnovolov.forgetmenot.persistence.shortterm.DeckFromFileCreatorStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class AddDeckDiScope private constructor(
    initialDeckAdderState: DeckFromFileCreator.State? = null,
    initialscreenState: AddDeckScreenState? = null
) {
    private val deckFromFileCreatorStateProvider = DeckFromFileCreatorStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val deckFromFileCreatorState: DeckFromFileCreator.State =
        initialDeckAdderState ?: deckFromFileCreatorStateProvider.load()

    private val deckFromFileCreator = DeckFromFileCreator(
        deckFromFileCreatorState,
        AppDiScope.get().globalState
    )

    private val deckCreator = DeckCreator(
        AppDiScope.get().globalState
    )

    private val screenStateProvider = AddDeckScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val screenState: AddDeckScreenState =
        initialscreenState ?: screenStateProvider.load()

    val controller = AddDeckController(
        screenState,
        deckCreator,
        deckFromFileCreator,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        deckFromFileCreatorStateProvider,
        screenStateProvider
    )

    val viewModel = AddDeckViewModel(
        deckFromFileCreatorState,
        screenState,
        AppDiScope.get().globalState
    )

    companion object : DiScopeManager<AddDeckDiScope>() {
        fun create(
            initialDeckFromFileCreatorState: DeckFromFileCreator.State,
            initialAddDeckScreenState: AddDeckScreenState
        ) = AddDeckDiScope(
            initialDeckFromFileCreatorState,
            initialAddDeckScreenState
        )

        override fun recreateDiScope() = AddDeckDiScope()

        override fun onCloseDiScope(diScope: AddDeckDiScope) {
            diScope.controller.dispose()
        }
    }
}