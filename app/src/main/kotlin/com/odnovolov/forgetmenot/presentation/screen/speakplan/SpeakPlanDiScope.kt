package com.odnovolov.forgetmenot.presentation.screen.speakplan

import com.odnovolov.forgetmenot.domain.interactor.decksettings.SpeakPlanSettings
import com.odnovolov.forgetmenot.persistence.shortterm.PresetDialogStateProvider
import com.odnovolov.forgetmenot.persistence.shortterm.SpeakEventDialogStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.common.customview.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.screen.decksetup.decksettings.DeckSettingsDiScope

class SpeakPlanDiScope private constructor(
    initialPresetDialogState: PresetDialogState? = null,
    initialSpeakEventDialogState: SpeakEventDialogState? = null
) {
    private val presetDialogStateProvider = PresetDialogStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        key = "SpeakPlan Preset State"
    )

    private val presetDialogState: PresetDialogState =
        initialPresetDialogState ?: presetDialogStateProvider.load()

    private val speakEventDialogStateProvider = SpeakEventDialogStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val speakEventDialogState: SpeakEventDialogState =
        initialSpeakEventDialogState ?: speakEventDialogStateProvider.load()

    private val speakPlanSettings = SpeakPlanSettings(
        DeckSettingsDiScope.shareDeckSettings(),
        AppDiScope.get().globalState
    )

    val presetController = SpeakPlanPresetController(
        DeckSettingsDiScope.shareDeckSettings().state,
        speakPlanSettings,
        presetDialogState,
        AppDiScope.get().globalState,
        AppDiScope.get().longTermStateSaver,
        presetDialogStateProvider
    )

    val presetViewModel = SpeakPlanPresetViewModel(
        DeckSettingsDiScope.shareDeckSettings().state,
        presetDialogState,
        AppDiScope.get().globalState
    )

    val controller = SpeakPlanController(
        DeckSettingsDiScope.shareDeckSettings().state,
        speakPlanSettings,
        speakEventDialogState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        speakEventDialogStateProvider
    )

    val viewModel = SpeakPlanViewModel(
        DeckSettingsDiScope.shareDeckSettings().state,
        speakEventDialogState
    )

    companion object : DiScopeManager<SpeakPlanDiScope>() {
        fun create(
            initialPresetDialogState: PresetDialogState,
            initialSpeakEventDialogState: SpeakEventDialogState
        ) = SpeakPlanDiScope(
            initialPresetDialogState,
            initialSpeakEventDialogState
        )

        override fun recreateDiScope() = SpeakPlanDiScope()

        override fun onCloseDiScope(diScope: SpeakPlanDiScope) {
            diScope.controller.dispose()
        }
    }
}