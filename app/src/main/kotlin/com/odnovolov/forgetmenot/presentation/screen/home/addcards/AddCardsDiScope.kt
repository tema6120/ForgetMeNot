package com.odnovolov.forgetmenot.presentation.screen.home.addcards

import com.odnovolov.forgetmenot.domain.interactor.deckcreator.DeckCreator
import com.odnovolov.forgetmenot.persistence.shortterm.AddDeckScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class AddCardsDiScope private constructor(
    initialScreenState: AddCardsScreenState? = null
) {
    private val deckCreator = DeckCreator(
        AppDiScope.get().globalState
    )

    private val screenStateProvider = AddDeckScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val screenState: AddCardsScreenState =
        initialScreenState ?: screenStateProvider.load()

    private val fileFromIntentReader = FileFromIntentReader(
        AppDiScope.get().app.contentResolver
    )

    val controller = AddCardsController(
        screenState,
        deckCreator,
        fileFromIntentReader,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        screenStateProvider
    )

    val viewModel = AddCardsViewModel(
        screenState,
        AppDiScope.get().globalState
    )

    companion object : DiScopeManager<AddCardsDiScope>() {
        fun create(initialAddCardsScreenState: AddCardsScreenState) =
            AddCardsDiScope(initialAddCardsScreenState)

        override fun recreateDiScope() = AddCardsDiScope()

        override fun onCloseDiScope(diScope: AddCardsDiScope) {
            diScope.controller.dispose()
        }
    }
}