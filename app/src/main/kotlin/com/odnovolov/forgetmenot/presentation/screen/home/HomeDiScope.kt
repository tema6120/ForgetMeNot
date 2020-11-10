package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.interactor.deckexporter.DeckExporter
import com.odnovolov.forgetmenot.domain.interactor.deckremover.DeckRemover
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseStateCreator
import com.odnovolov.forgetmenot.domain.interactor.searcher.CardsSearcher
import com.odnovolov.forgetmenot.persistence.longterm.deckreviewpreference.DeckReviewPreferenceProvider
import com.odnovolov.forgetmenot.persistence.shortterm.HomeScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class HomeDiScope private constructor(
    initialHomeScreenState: HomeScreenState? = null
) {
    private val deckReviewPreference: DeckReviewPreference =
        DeckReviewPreferenceProvider(AppDiScope.get().database).load()

    private val homeScreenStateProvider = HomeScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState
    )

    private val homeScreenState: HomeScreenState =
        initialHomeScreenState ?: homeScreenStateProvider.load()

    private val deckRemover = DeckRemover(AppDiScope.get().globalState)

    private val exerciseStateCreator = ExerciseStateCreator(AppDiScope.get().globalState)

    private val cardsSearcher = CardsSearcher(
        AppDiScope.get().globalState
    )

    val controller = HomeController(
        homeScreenState,
        deckReviewPreference,
        DeckExporter(),
        deckRemover,
        exerciseStateCreator,
        cardsSearcher,
        AppDiScope.get().globalState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        homeScreenStateProvider
    )

    val viewModel = HomeViewModel(
        homeScreenState,
        AppDiScope.get().globalState,
        deckReviewPreference,
        controller,
        cardsSearcher.state
    )

    companion object : DiScopeManager<HomeDiScope>() {
        fun create(initialHomeScreenState: HomeScreenState) = HomeDiScope(initialHomeScreenState)

        fun shareDeckReviewPreference(): DeckReviewPreference {
            return diScope?.deckReviewPreference ?: error("HomeDiScope is not opened")
        }

        override fun recreateDiScope() = HomeDiScope()

        override fun onCloseDiScope(diScope: HomeDiScope) {
            diScope.controller.dispose()
            diScope.cardsSearcher.dispose()
        }
    }
}