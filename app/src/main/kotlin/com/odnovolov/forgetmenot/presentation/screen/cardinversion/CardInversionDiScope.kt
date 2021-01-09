package com.odnovolov.forgetmenot.presentation.screen.cardinversion

import com.odnovolov.forgetmenot.persistence.shortterm.CardInversionScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.exampleexercise.ExampleExerciseDiScope

class CardInversionDiScope private constructor(
    initialScreenState: CardInversionScreenState? = null
) {
    private val screenStateProvider = CardInversionScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val screenState: CardInversionScreenState =
        initialScreenState ?: screenStateProvider.load()

    val controller = CardInversionController(
        DeckSettingsDiScope.getOrRecreate().deckSettings,
        ExampleExerciseDiScope.getOrRecreate().exercise,
        screenState,
        AppDiScope.get().longTermStateSaver,
        screenStateProvider
    )

    val viewModel = CardInversionViewModel(
        DeckSettingsDiScope.getOrRecreate().deckSettings.state,
        screenState,
    )

    companion object : DiScopeManager<CardInversionDiScope>() {
        fun create(initialScreenState: CardInversionScreenState) =
            CardInversionDiScope(initialScreenState)

        override fun recreateDiScope() = CardInversionDiScope()

        override fun onCloseDiScope(diScope: CardInversionDiScope) {
            diScope.controller.dispose()
        }
    }
}