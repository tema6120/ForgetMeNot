package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.BatchCardEditor
import com.odnovolov.forgetmenot.domain.interactor.operationsondecks.DeckRemover
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseStateCreator
import com.odnovolov.forgetmenot.domain.interactor.operationsondecks.DeckMerger
import com.odnovolov.forgetmenot.domain.interactor.searcher.CardsSearcher
import com.odnovolov.forgetmenot.persistence.longterm.deckreviewpreference.DeckReviewPreferenceProvider
import com.odnovolov.forgetmenot.persistence.shortterm.HomeScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class HomeDiScope private constructor(
    initialHomeScreenState: HomeScreenState? = null,
    initialBatchCardEditor: BatchCardEditor? = null
) {
    private val deckReviewPreference: DeckReviewPreference =
        DeckReviewPreferenceProvider(AppDiScope.get().database).load()

    private val homeScreenStateProvider = HomeScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState,
        AppDiScope.get().fileImportStorage
    )

    val screenState: HomeScreenState =
        initialHomeScreenState ?: homeScreenStateProvider.load()

    private val deckRemover = DeckRemover(
        AppDiScope.get().globalState
    )

    private val deckMerger = DeckMerger(
        AppDiScope.get().globalState,
        coroutineContext = businessLogicThread
    )

    private val exerciseStateCreator = ExerciseStateCreator(
        AppDiScope.get().globalState
    )

    private val cardsSearcher = CardsSearcher(
        AppDiScope.get().globalState
    )

    val batchCardEditor: BatchCardEditor =
        initialBatchCardEditor ?: TODO()

    val controller = HomeController(
        screenState,
        deckReviewPreference,
        deckRemover,
        deckMerger,
        exerciseStateCreator,
        cardsSearcher,
        batchCardEditor,
        AppDiScope.get().globalState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        homeScreenStateProvider
    )

    val viewModel = HomeViewModel(
        screenState,
        AppDiScope.get().globalState,
        deckReviewPreference,
        controller,
        cardsSearcher.state,
        batchCardEditor.state
    )

    companion object : DiScopeManager<HomeDiScope>() {
        fun create(
            initialHomeScreenState: HomeScreenState,
            batchCardEditor: BatchCardEditor
        ) = HomeDiScope(
            initialHomeScreenState,
            batchCardEditor
        )

        override fun recreateDiScope() = HomeDiScope()

        override fun onCloseDiScope(diScope: HomeDiScope) {
            with(diScope) {
                controller.dispose()
                cardsSearcher.dispose()
                deckMerger.dispose()
            }
        }
    }
}