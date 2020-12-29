package com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.motivationaltimer

import com.odnovolov.forgetmenot.persistence.shortterm.MotivationalTimerDialogStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope

class MotivationalTimerDiScope private constructor(
    initialDialogState: MotivationalTimerDialogState? = null
) {
    private val dialogStateProvider = MotivationalTimerDialogStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val dialogState: MotivationalTimerDialogState =
        initialDialogState ?: dialogStateProvider.load()

    val controller = MotivationalTimerController(
        DeckSettingsDiScope.get()!!.deckSettings,
        dialogState,
        AppDiScope.get().longTermStateSaver,
        dialogStateProvider
    )

    val viewModel = MotivationalTimerViewModel(
        dialogState
    )

    companion object : DiScopeManager<MotivationalTimerDiScope>() {
        fun create(initialDialogState: MotivationalTimerDialogState) =
            MotivationalTimerDiScope(initialDialogState)

        override fun recreateDiScope() = MotivationalTimerDiScope()

        override fun onCloseDiScope(diScope: MotivationalTimerDiScope) {
            diScope.controller.dispose()
        }
    }
}