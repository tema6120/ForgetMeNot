package com.odnovolov.forgetmenot.presentation.screen.search

import com.odnovolov.forgetmenot.persistence.shortterm.SearchScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class SearchDiScope private constructor(
    initialScreenState: SearchScreenState? = null
) {
    private val screenStateProvider = SearchScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val screenState: SearchScreenState =
        initialScreenState ?: screenStateProvider.load()

    val controller = SearchController(
        screenState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        screenStateProvider
    )

    val viewModel = SearchViewModel(
        screenState,
        AppDiScope.get().globalState
    )

    companion object : DiScopeManager<SearchDiScope>() {
        fun create(screenState: SearchScreenState) = SearchDiScope(screenState)

        override fun recreateDiScope() = SearchDiScope()

        override fun onCloseDiScope(diScope: SearchDiScope) {
            diScope.controller.dispose()
        }
    }
}