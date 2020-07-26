package com.odnovolov.forgetmenot.presentation.screen.search

import com.odnovolov.forgetmenot.domain.interactor.searcher.Searcher
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.decksetup.DeckSetupDiScope

class SearchDiScope(
    initialSearchText: String = ""
) {
    private val searcher: Searcher =
        if (DeckSetupDiScope.isOpen()) {
            val deck = DeckSetupDiScope.shareDeck()
            Searcher(deck)
        } else {
            val globalState = AppDiScope.get().globalState
            Searcher(globalState)
        }

    val controller = SearchController(
        searcher,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = SearchViewModel(
        initialSearchText,
        searcher.state
    )

    companion object : DiScopeManager<SearchDiScope>() {
        override fun recreateDiScope() = SearchDiScope()

        override fun onCloseDiScope(diScope: SearchDiScope) {
            diScope.controller.dispose()
            diScope.searcher.dispose()
        }
    }
}