package com.odnovolov.forgetmenot.presentation.screen.cardappearance

import com.odnovolov.forgetmenot.persistence.shortterm.CardAppearanceScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.example.CardAppearanceExampleViewModel
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.textopacitydialog.CardTexOpacityController
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.textopacitydialog.CardTexOpacityViewModel
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.textsizedialog.CardTextSizeController
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.textsizedialog.CardTextSizeViewModel

class CardAppearanceDiScope private constructor(
    val initialScreenState: CardAppearanceScreenState? = null
) {
    private val cardAppearance: CardAppearance = AppDiScope.get().cardAppearance

    private val screenStateProvider = CardAppearanceScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState
    )

    private val screenState: CardAppearanceScreenState =
        initialScreenState ?: screenStateProvider.load()

    val controller = CardAppearanceController(
        cardAppearance,
        screenState,
        AppDiScope.get().longTermStateSaver,
        screenStateProvider
    )

    val viewModel = CardAppearanceViewModel(
        cardAppearance
    )

    val cardTextSizeController = CardTextSizeController(
        cardAppearance,
        screenState,
        AppDiScope.get().longTermStateSaver,
        screenStateProvider
    )

    val cardTextSizeViewModel = CardTextSizeViewModel(
        screenState
    )

    val cardTexOpacityController = CardTexOpacityController(
        cardAppearance,
        screenState,
        AppDiScope.get().longTermStateSaver,
        screenStateProvider
    )

    val cardTexOpacityViewModel = CardTexOpacityViewModel(
        screenState
    )

    val exampleViewModel = CardAppearanceExampleViewModel(
        cardAppearance,
        screenState
    )

    companion object : DiScopeManager<CardAppearanceDiScope>() {
        fun create(screenState: CardAppearanceScreenState) = CardAppearanceDiScope(screenState)

        override fun recreateDiScope() = CardAppearanceDiScope()

        override fun onCloseDiScope(diScope: CardAppearanceDiScope) {
            diScope.controller.dispose()
            diScope.cardTextSizeController.dispose()
        }
    }
}