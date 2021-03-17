package com.odnovolov.forgetmenot.presentation.screen.cardappearance

import com.odnovolov.forgetmenot.persistence.longterm.cardappearance.CardAppearanceProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class CardAppearanceDiScope {
    private val cardAppearance: CardAppearance = CardAppearanceProvider(
        AppDiScope.get().database
    ).load()

    val controller = CardAppearanceController(
        cardAppearance,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = CardAppearanceViewModel(
        cardAppearance
    )

    companion object : DiScopeManager<CardAppearanceDiScope>() {
        override fun recreateDiScope() = CardAppearanceDiScope()

        override fun onCloseDiScope(diScope: CardAppearanceDiScope) {
            diScope.controller.dispose()
        }
    }
}