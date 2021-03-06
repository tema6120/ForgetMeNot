package com.odnovolov.forgetmenot.presentation.screen.search

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.BatchCardEditor
import com.odnovolov.forgetmenot.domain.interactor.searcher.CardsSearcher
import com.odnovolov.forgetmenot.persistence.shortterm.CardsSearcherProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class SearchDiScope private constructor(
    initialCardsSearcher: CardsSearcher? = null,
    initialBatchCardEditor: BatchCardEditor? = null,
    initialSearchText: String = ""
) {
    private val cardsSearcherProvider = CardsSearcherProvider(
        AppDiScope.get().globalState,
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    init {
        if (initialCardsSearcher != null) {
            cardsSearcherProvider.save(initialCardsSearcher)
        }
    }

    private val cardsSearcher: CardsSearcher =
        initialCardsSearcher ?: cardsSearcherProvider.load()

    val batchCardEditor: BatchCardEditor =
        initialBatchCardEditor ?: TODO()

    val controller = SearchController(
        cardsSearcher,
        batchCardEditor,
        AppDiScope.get().navigator,
        AppDiScope.get().globalState,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = SearchViewModel(
        initialSearchText,
        cardsSearcher.state,
        batchCardEditor.state
    )

    companion object : DiScopeManager<SearchDiScope>() {
        fun create(
            cardsSearcher: CardsSearcher,
            batchCardEditor: BatchCardEditor,
            initialSearchText: String = ""
        ) = SearchDiScope(
            cardsSearcher,
            batchCardEditor,
            initialSearchText
        )

        override fun recreateDiScope() = SearchDiScope()

        override fun onCloseDiScope(diScope: SearchDiScope) {
            diScope.controller.dispose()
            diScope.cardsSearcher.dispose()
            diScope.batchCardEditor.finish()
        }
    }
}