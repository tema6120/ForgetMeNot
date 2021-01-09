package com.odnovolov.forgetmenot.presentation.screen.pronunciationplan

import com.odnovolov.forgetmenot.domain.interactor.decksettings.PronunciationPlanSettings
import com.odnovolov.forgetmenot.persistence.shortterm.PronunciationEventDialogStateProvider
import com.odnovolov.forgetmenot.persistence.shortterm.PronunciationPlanScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope

class PronunciationPlanDiScope private constructor(
    initialScreenState: PronunciationPlanScreenState? = null,
    initialPronunciationEventDialogState: PronunciationEventDialogState? = null
) {
    private val screenStateProvider = PronunciationPlanScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val screenState: PronunciationPlanScreenState =
        initialScreenState ?: screenStateProvider.load()

    private val pronunciationEventDialogStateProvider = PronunciationEventDialogStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val pronunciationEventDialogState: PronunciationEventDialogState =
        initialPronunciationEventDialogState ?: pronunciationEventDialogStateProvider.load()

    private val pronunciationPlanSettings = PronunciationPlanSettings(
        DeckSettingsDiScope.getOrRecreate().deckSettings
    )

    val controller = PronunciationPlanController(
        DeckSettingsDiScope.getOrRecreate().deckSettings.state,
        pronunciationPlanSettings,
        screenState,
        pronunciationEventDialogState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        screenStateProvider,
        pronunciationEventDialogStateProvider
    )

    val viewModel = PronunciationPlanViewModel(
        DeckSettingsDiScope.getOrRecreate().deckSettings.state,
        screenState,
        pronunciationEventDialogState
    )

    companion object : DiScopeManager<PronunciationPlanDiScope>() {
        fun create(
            initialScreenState: PronunciationPlanScreenState,
            initialPronunciationEventDialogState: PronunciationEventDialogState
        ) = PronunciationPlanDiScope(
            initialScreenState,
            initialPronunciationEventDialogState
        )

        override fun recreateDiScope() = PronunciationPlanDiScope()

        override fun onCloseDiScope(diScope: PronunciationPlanDiScope) {
            diScope.controller.dispose()
        }
    }
}