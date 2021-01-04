package com.odnovolov.forgetmenot.presentation.screen.cardinversion

import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.example.ExampleExerciseDiScope

class CardInversionDiScope {
    val controller = CardInversionController(
        DeckSettingsDiScope.get()!!.deckSettings,
        ExampleExerciseDiScope.get()!!.exercise,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = CardInversionViewModel(
        DeckSettingsDiScope.get()!!.deckSettings.state
    )

    companion object : DiScopeManager<CardInversionDiScope>() {
        override fun recreateDiScope() = CardInversionDiScope()

        override fun onCloseDiScope(diScope: CardInversionDiScope) {
            diScope.controller.dispose()
        }
    }
}