package com.odnovolov.forgetmenot.presentation.screen.helparticle

import com.odnovolov.forgetmenot.persistence.longterm.helpscreenstate.HelpScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class HelpArticleDiScope(
    helpArticle: HelpArticle? = null
) {
    private val screenState: HelpArticleScreenState = HelpScreenStateProvider(
        AppDiScope.get().database
    ).load()

    init {
        if (helpArticle != null) {
            screenState.currentArticle = helpArticle
        }
    }

    val controller = HelpArticleController(
        screenState,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = HelpArticleViewModel(
        screenState
    )

    companion object : DiScopeManager<HelpArticleDiScope>() {
        override fun recreateDiScope() = HelpArticleDiScope()

        override fun onCloseDiScope(diScope: HelpArticleDiScope) {
            diScope.controller.dispose()
        }
    }
}