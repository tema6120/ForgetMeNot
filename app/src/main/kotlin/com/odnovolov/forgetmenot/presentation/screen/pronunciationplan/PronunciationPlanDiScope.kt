package com.odnovolov.forgetmenot.presentation.screen.pronunciationplan

import com.odnovolov.forgetmenot.domain.interactor.decksettings.PronunciationPlanSettings
import com.odnovolov.forgetmenot.persistence.shortterm.PronunciationEventDialogStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope

class PronunciationPlanDiScope private constructor(
    initialPronunciationEventDialogState: PronunciationEventDialogState? = null
) {
    private val pronunciationEventDialogStateProvider = PronunciationEventDialogStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val pronunciationEventDialogState: PronunciationEventDialogState =
        initialPronunciationEventDialogState ?: pronunciationEventDialogStateProvider.load()

    private val pronunciationPlanSettings = PronunciationPlanSettings(
        DeckSettingsDiScope.get()!!.deckSettings
    )

    val controller = PronunciationPlanController(
        DeckSettingsDiScope.get()!!.deckSettings.state,
        pronunciationPlanSettings,
        pronunciationEventDialogState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        pronunciationEventDialogStateProvider
    )

    val viewModel = PronunciationPlanViewModel(
        DeckSettingsDiScope.get()!!.deckSettings.state,
        pronunciationEventDialogState
    )

    companion object : DiScopeManager<PronunciationPlanDiScope>() {
        fun create(
            initialPronunciationEventDialogState: PronunciationEventDialogState
        ) = PronunciationPlanDiScope(
            initialPronunciationEventDialogState
        )

        override fun recreateDiScope() = PronunciationPlanDiScope()

        override fun onCloseDiScope(diScope: PronunciationPlanDiScope) {
            diScope.controller.dispose()
        }
    }
}