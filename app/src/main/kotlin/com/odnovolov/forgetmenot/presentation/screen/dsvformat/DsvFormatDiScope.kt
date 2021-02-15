package com.odnovolov.forgetmenot.presentation.screen.dsvformat

import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class DsvFormatDiScope private constructor(
    initialScreenState: DsvFormatScreenState? = null
) {
    val screenState: DsvFormatScreenState =
        initialScreenState ?: throw NullPointerException()

    val controller = DsvFormatController(
        screenState,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = DsvFormatViewModel(
        screenState
    )

    companion object : DiScopeManager<DsvFormatDiScope>() {
        fun create(screenState: DsvFormatScreenState) = DsvFormatDiScope(screenState)

        override fun recreateDiScope() = DsvFormatDiScope()

        override fun onCloseDiScope(diScope: DsvFormatDiScope) {
            diScope.controller.dispose()
        }
    }
}