package com.odnovolov.forgetmenot.presentation.screen.cardfilterforautoplay

import com.odnovolov.forgetmenot.domain.interactor.autoplay.PlayerStateCreator
import com.odnovolov.forgetmenot.persistence.shortterm.PlayerCreatorStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class CardFilterForAutoplayDiScope private constructor(
    initialPlayerCreatorState: PlayerStateCreator.State? = null
) {
    private val playerCreatorStateProvider = PlayerCreatorStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState
    )

    private val playerCreatorState: PlayerStateCreator.State =
        initialPlayerCreatorState ?: playerCreatorStateProvider.load()

    private val playerStateCreator = PlayerStateCreator(
        playerCreatorState,
        AppDiScope.get().globalState
    )

    val controller = CardFilterForAutoplayController(
        playerStateCreator,
        AppDiScope.get().globalState.cardFilterForAutoplay,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        playerCreatorStateProvider
    )

    val viewModel = CardFilterForAutoplayViewModel(
        playerStateCreator,
        AppDiScope.get().globalState
    )

    companion object : DiScopeManager<CardFilterForAutoplayDiScope>() {
        fun create(initialPlayerCreatorState: PlayerStateCreator.State) =
            CardFilterForAutoplayDiScope(initialPlayerCreatorState)

        override fun recreateDiScope() = CardFilterForAutoplayDiScope()

        override fun onCloseDiScope(diScope: CardFilterForAutoplayDiScope) {
            diScope.controller.dispose()
        }
    }
}