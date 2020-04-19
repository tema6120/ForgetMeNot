package com.odnovolov.forgetmenot.presentation.screen.speakplan

import com.odnovolov.forgetmenot.domain.interactor.decksettings.SpeakPlanSettings
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsDiScope

class SpeakPlanDiScope(
    initialDialogState: SpeakEventDialogState
) {
    // todo: create provider
    private val dialogState: SpeakEventDialogState = initialDialogState

    private val speakPlanSettings = SpeakPlanSettings(
        DeckSettingsDiScope.shareDeckSettings(),
        AppDiScope.get().globalState
    )

    val controller = SpeakPlanController(
        DeckSettingsDiScope.shareDeckSettings().state,
        speakPlanSettings,
        dialogState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = SpeakPlanViewModel(
        DeckSettingsDiScope.shareDeckSettings().state,
        dialogState
    )

    companion object : DiScopeManager<SpeakPlanDiScope>() {
        override fun recreateDiScope() = SpeakPlanDiScope(SpeakEventDialogState())

        override fun onCloseDiScope(diScope: SpeakPlanDiScope) {
            diScope.controller.dispose()
        }
    }
}