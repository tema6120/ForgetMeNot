package com.odnovolov.forgetmenot.presentation.screen.helparticle

import com.odnovolov.forgetmenot.persistence.shortterm.HelpArticleScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class HelpArticleDiScope private constructor(
    helpArticleScreenState: HelpArticleScreenState? = null
) {
    private val screenStateProvider = HelpArticleScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val screenState: HelpArticleScreenState =
        helpArticleScreenState ?: screenStateProvider.load()

    val controller = HelpArticleController(
        screenState,
        AppDiScope.get().longTermStateSaver,
        screenStateProvider
    )

    val viewModel = HelpArticleViewModel(
        screenState
    )

    companion object : DiScopeManager<HelpArticleDiScope>() {
        fun create(screenState: HelpArticleScreenState) = HelpArticleDiScope(screenState)

        override fun recreateDiScope() = HelpArticleDiScope()

        override fun onCloseDiScope(diScope: HelpArticleDiScope) {
            diScope.controller.dispose()
        }
    }
}