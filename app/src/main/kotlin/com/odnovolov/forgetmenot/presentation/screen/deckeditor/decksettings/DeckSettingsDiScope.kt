package com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings

import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.persistence.shortterm.PresetDialogStateProvider
import com.odnovolov.forgetmenot.presentation.common.customview.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorDiScope

class DeckSettingsDiScope private constructor(
    initialPresetDialogState: PresetDialogState? = null
) {
    private val deckSettingsState = DeckSettings.State(
        DeckEditorDiScope.shareDeck()
    )

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
        AppDiScope.get().navigator,
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
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = DeckSettingsViewModel(
        deckSettingsState
    )

    companion object : DiScopeManager<DeckSettingsDiScope>() {
        fun create(initialPresetDialogState: PresetDialogState) =
            DeckSettingsDiScope(initialPresetDialogState)

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