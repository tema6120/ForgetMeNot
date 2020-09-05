package com.odnovolov.forgetmenot.presentation.screen.help

import com.odnovolov.forgetmenot.persistence.longterm.helpscreenstate.HelpScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class HelpDiScope(
    helpArticle: HelpArticle? = null
) {
    private val screenState: HelpScreenState = HelpScreenStateProvider(
        AppDiScope.get().database
    ).load()

    init {
        if (helpArticle != null) {
            screenState.currentArticle = helpArticle
        }
    }

    val controller = HelpController(
        screenState,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = HelpViewModel(
        screenState
    )

    companion object : DiScopeManager<HelpDiScope>() {
        override fun recreateDiScope() = HelpDiScope()

        override fun onCloseDiScope(diScope: HelpDiScope) {
            diScope.controller.dispose()
        }
    }
}