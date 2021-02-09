package com.odnovolov.forgetmenot.presentation.screen.home.addcards

import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class AddCardsDiScope {
    private val fileFromIntentReader = FileFromIntentReader(
        AppDiScope.get().app.contentResolver
    )

    val controller = AddCardsController(
        fileFromIntentReader,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver
    )

    companion object : DiScopeManager<AddCardsDiScope>() {
        override fun recreateDiScope() = AddCardsDiScope()

        override fun onCloseDiScope(diScope: AddCardsDiScope) {
            diScope.controller.dispose()
        }
    }
}