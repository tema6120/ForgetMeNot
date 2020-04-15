package com.odnovolov.forgetmenot.presentation.screen.decksettings

import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.persistence.shortterm.DeckSettingsScreenStateProvider
import com.odnovolov.forgetmenot.persistence.shortterm.DeckSettingsStateProvider
import com.odnovolov.forgetmenot.persistence.shortterm.PresetDialogStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.common.preset.PresetDialogState

class DeckSettingsDiScope private constructor(
    initialDeckSettingsState: DeckSettings.State? = null,
    initialDeckSettingsScreenState: DeckSettingsScreenState? = null,
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

    private val deckSettingsScreenStateProvider = DeckSettingsScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val deckSettingsScreenState =
        initialDeckSettingsScreenState ?: deckSettingsScreenStateProvider.load()

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
        deckSettingsScreenState,
        deckSettings,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        deckSettingsStateProvider,
        deckSettingsScreenStateProvider
    )

    val viewModel = DeckSettingsViewModel(
        deckSettingsScreenState,
        deckSettingsState,
        AppDiScope.get().globalState
    )

    companion object : DiScopeManager<DeckSettingsDiScope>() {
        fun create(
            initialDeckSettingsState: DeckSettings.State,
            initialDeckSettingsScreenState: DeckSettingsScreenState,
            initialPresetDialogState: PresetDialogState
        ) = DeckSettingsDiScope(
            initialDeckSettingsState,
            initialDeckSettingsScreenState,
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