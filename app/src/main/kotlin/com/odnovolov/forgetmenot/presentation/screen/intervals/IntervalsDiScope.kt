package com.odnovolov.forgetmenot.presentation.screen.intervals

import com.odnovolov.forgetmenot.domain.interactor.decksettings.IntervalsSettings
import com.odnovolov.forgetmenot.persistence.shortterm.PresetDialogStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.common.customview.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsDiScope

class IntervalsDiScope private constructor(
    initialPresetDialogState: PresetDialogState? = null
) {
    private val presetDialogStateProvider = PresetDialogStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        key = "IntervalScheme Preset State"
    )

    private val presetDialogState: PresetDialogState =
        initialPresetDialogState ?: presetDialogStateProvider.load()

    private val intervalsSettings = IntervalsSettings(
        DeckSettingsDiScope.shareDeckSettings(),
        AppDiScope.get().globalState
    )

    val presetController = IntervalsPresetController(
        DeckSettingsDiScope.shareDeckSettings().state,
        intervalsSettings,
        presetDialogState,
        AppDiScope.get().globalState,
        AppDiScope.get().longTermStateSaver,
        presetDialogStateProvider
    )

    val presetViewModel = IntervalsPresetViewModel(
        DeckSettingsDiScope.shareDeckSettings().state,
        presetDialogState,
        AppDiScope.get().globalState
    )

    val controller = IntervalsController(
        DeckSettingsDiScope.shareDeckSettings().state,
        intervalsSettings,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = IntervalsViewModel(
        DeckSettingsDiScope.shareDeckSettings().state
    )

    val adapter = IntervalAdapter(controller)

    companion object : DiScopeManager<IntervalsDiScope>() {
        fun create(initialPresetDialogState: PresetDialogState) =
            IntervalsDiScope(initialPresetDialogState)

        fun shareIntervalsSettings(): IntervalsSettings {
            return diScope?.intervalsSettings ?: error("IntervalsDiScope is not opened")
        }

        override fun recreateDiScope() = IntervalsDiScope()

        override fun onCloseDiScope(diScope: IntervalsDiScope) {
            diScope.controller.dispose()
            diScope.presetController.dispose()
        }
    }
}