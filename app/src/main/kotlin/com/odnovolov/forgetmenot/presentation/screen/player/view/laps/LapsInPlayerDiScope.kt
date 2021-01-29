package com.odnovolov.forgetmenot.presentation.screen.player.view.laps

import com.odnovolov.forgetmenot.persistence.shortterm.LapsInPlayerDialogStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.player.PlayerDiScope

class LapsInPlayerDiScope private constructor(
    initialLapsInPlayerDialogState: LapsInPlayerDialogState? = null
) {
    private val dialogStateProvider = LapsInPlayerDialogStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val dialogState: LapsInPlayerDialogState =
        initialLapsInPlayerDialogState ?: dialogStateProvider.load()

    val controller = LapsInPlayerController(
        PlayerDiScope.getOrRecreate().player,
        dialogState,
        dialogStateProvider,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = LapsInPlayerViewModel(
        dialogState
    )

    companion object : DiScopeManager<LapsInPlayerDiScope>() {
        fun create(dialogState: LapsInPlayerDialogState) = LapsInPlayerDiScope(dialogState)

        override fun recreateDiScope() = LapsInPlayerDiScope()

        override fun onCloseDiScope(diScope: LapsInPlayerDiScope) {
            diScope.controller.dispose()
        }
    }
}