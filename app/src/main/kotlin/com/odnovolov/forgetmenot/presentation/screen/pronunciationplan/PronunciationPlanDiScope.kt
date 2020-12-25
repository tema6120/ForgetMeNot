package com.odnovolov.forgetmenot.presentation.screen.pronunciationplan

import com.odnovolov.forgetmenot.domain.interactor.decksettings.PronunciationPlanSettings
import com.odnovolov.forgetmenot.persistence.shortterm.PresetDialogStateProvider
import com.odnovolov.forgetmenot.persistence.shortterm.PronunciationEventDialogStateProvider
import com.odnovolov.forgetmenot.presentation.common.customview.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope

class PronunciationPlanDiScope private constructor(
    initialPresetDialogState: PresetDialogState? = null,
    initialPronunciationEventDialogState: PronunciationEventDialogState? = null
) {
    private val presetDialogStateProvider = PresetDialogStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        key = "PronunciationPlan Preset State"
    )

    private val presetDialogState: PresetDialogState =
        initialPresetDialogState ?: presetDialogStateProvider.load()

    private val pronunciationEventDialogStateProvider = PronunciationEventDialogStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val pronunciationEventDialogState: PronunciationEventDialogState =
        initialPronunciationEventDialogState ?: pronunciationEventDialogStateProvider.load()

    private val pronunciationPlanSettings = PronunciationPlanSettings(
        DeckSettingsDiScope.shareDeckSettings(),
        AppDiScope.get().globalState
    )

    val presetController = PronunciationPlanPresetController(
        DeckSettingsDiScope.shareDeckSettings().state,
        pronunciationPlanSettings,
        presetDialogState,
        AppDiScope.get().globalState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        presetDialogStateProvider
    )

    val presetViewModel = PronunciationPlanPresetViewModel(
        DeckSettingsDiScope.shareDeckSettings().state,
        presetDialogState,
        AppDiScope.get().globalState
    )

    val controller = PronunciationPlanController(
        DeckSettingsDiScope.shareDeckSettings().state,
        pronunciationPlanSettings,
        pronunciationEventDialogState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        pronunciationEventDialogStateProvider
    )

    val viewModel = PronunciationPlanViewModel(
        DeckSettingsDiScope.shareDeckSettings().state,
        pronunciationEventDialogState
    )

    companion object : DiScopeManager<PronunciationPlanDiScope>() {
        fun create(
            initialPresetDialogState: PresetDialogState,
            initialPronunciationEventDialogState: PronunciationEventDialogState
        ) = PronunciationPlanDiScope(
            initialPresetDialogState,
            initialPronunciationEventDialogState
        )

        override fun recreateDiScope() = PronunciationPlanDiScope()

        override fun onCloseDiScope(diScope: PronunciationPlanDiScope) {
            diScope.controller.dispose()
        }
    }
}