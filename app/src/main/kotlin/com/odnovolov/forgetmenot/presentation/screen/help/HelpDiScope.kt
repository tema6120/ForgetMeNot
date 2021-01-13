package com.odnovolov.forgetmenot.presentation.screen.help

import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class HelpDiScope {
    val controller = HelpController(
        AppDiScope.get().navigator
    )

    companion object : DiScopeManager<HelpDiScope>() {
        override fun recreateDiScope() = HelpDiScope()

        override fun onCloseDiScope(diScope: HelpDiScope) {
            diScope.controller.dispose()
        }
    }
}