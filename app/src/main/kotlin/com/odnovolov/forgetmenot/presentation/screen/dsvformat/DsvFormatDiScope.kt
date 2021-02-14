package com.odnovolov.forgetmenot.presentation.screen.dsvformat

import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class DsvFormatDiScope {
    val controller = DsvFormatController(
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = DsvFormatViewModel(

    )

    companion object : DiScopeManager<DsvFormatDiScope>() {
        override fun recreateDiScope() = DsvFormatDiScope()

        override fun onCloseDiScope(diScope: DsvFormatDiScope) {
            diScope.controller.dispose()
        }
    }
}