package com.odnovolov.forgetmenot.presentation.screen.decksettings

import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.persistence.shortterm.DeckSettingsStateProvider
import com.odnovolov.forgetmenot.persistence.shortterm.PresetDialogStateProvider
import com.odnovolov.forgetmenot.presentation.common.customview.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class DeckSettingsDiScope private constructor(
    initialDeckSettingsState: DeckSettings.State? = null,
    initialPresetDialogState: PresetDialogState? = null
) {
    private val deckSettingsStateProvider = DeckSettingsStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState
    )

    private val deckSettingsState: DeckSettings.State =
        initialDeckSettingsState ?: deckSettingsStateProvider.load()

    private val presetDialogStateProvider = PresetDialogStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        key = "ExercisePreference Preset State"
    )

    private val presetDialogState: PresetDialogState =
        initialPresetDialogState ?: presetDialogStateProvider.load()

    private val deckSettings = DeckSettings(
        deckSettingsState,
        AppDiScope.get().globalState
    )

    val presetController = ExercisePreferencePresetController(
        deckSettings,
        presetDialogState,
        AppDiScope.get().globalState,
        presetDialogStateProvider,
        AppDiScope.get().longTermStateSaver
    )

    val presetViewModel = ExercisePreferencePresetViewModel(
        presetDialogState,
        deckSettingsState,
        AppDiScope.get().globalState
    )

    val controller = DeckSettingsController(
        deckSettings,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        deckSettingsStateProvider
    )

    val viewModel = DeckSettingsViewModel(
        deckSettingsState
    )

    companion object : DiScopeManager<DeckSettingsDiScope>() {
        fun create(
            initialDeckSettingsState: DeckSettings.State,
            initialPresetDialogState: PresetDialogState
        ) = DeckSettingsDiScope(
            initialDeckSettingsState,
            initialPresetDialogState
        )

        fun shareDeckSettings(): DeckSettings {
            return diScope?.deckSettings ?: error("DeckSettingsDiScope is not opened")
        }

        override fun recreateDiScope() = DeckSettingsDiScope()

        override fun onCloseDiScope(diScope: DeckSettingsDiScope) {
            diScope.controller.dispose()
            diScope.presetController.dispose()
        }
    }
}