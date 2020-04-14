package com.odnovolov.forgetmenot.presentation.screen.home.decksorting

import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.home.HomeDiScope

class DeckSortingDiScope {
    val controller = DeckSortingController(
        HomeDiScope.shareDeckReviewPreference(),
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = DeckSortingViewModel(
        HomeDiScope.shareDeckReviewPreference()
    )

    companion object : DiScopeManager<DeckSortingDiScope>() {
        override fun recreateDiScope() = DeckSortingDiScope()

        override fun onCloseDiScope(diScope: DeckSortingDiScope) {
            diScope.controller.dispose()
        }
    }
}