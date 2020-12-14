package com.odnovolov.forgetmenot.presentation.screen.cardfiltersforautoplay

import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionStateCreator
import com.odnovolov.forgetmenot.persistence.shortterm.RepetitionCreatorStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class CardFiltersForAutoplayDiScope private constructor(
    initialRepetitionCreatorState: RepetitionStateCreator.State? = null
) {
    private val repetitionCreatorStateProvider = RepetitionCreatorStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState
    )

    private val repetitionCreatorState: RepetitionStateCreator.State =
        initialRepetitionCreatorState ?: repetitionCreatorStateProvider.load()

    private val repetitionStateCreator = RepetitionStateCreator(
        repetitionCreatorState,
        AppDiScope.get().globalState
    )

    val controller = CardFiltersForAutoplayController(
        repetitionStateCreator,
        AppDiScope.get().globalState.cardFiltersForAutoplay,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        repetitionCreatorStateProvider
    )

    val viewModel = CardFiltersForAutoplayViewModel(
        repetitionStateCreator,
        AppDiScope.get().globalState
    )

    companion object : DiScopeManager<CardFiltersForAutoplayDiScope>() {
        fun create(initialRepetitionCreatorState: RepetitionStateCreator.State) =
            CardFiltersForAutoplayDiScope(initialRepetitionCreatorState)

        override fun recreateDiScope() = CardFiltersForAutoplayDiScope()

        override fun onCloseDiScope(diScope: CardFiltersForAutoplayDiScope) {
            diScope.controller.dispose()
        }
    }
}