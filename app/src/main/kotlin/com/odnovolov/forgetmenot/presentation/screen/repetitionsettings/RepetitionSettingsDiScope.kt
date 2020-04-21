package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings

import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionSettings
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionStateCreator
import com.odnovolov.forgetmenot.persistence.shortterm.PresetDialogStateProvider
import com.odnovolov.forgetmenot.persistence.shortterm.RepetitionCreatorStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.common.customview.preset.PresetDialogState

class RepetitionSettingsDiScope private constructor(
    initialRepetitionCreatorState: RepetitionStateCreator.State? = null,
    initialPresetDialogState: PresetDialogState? = null
) {
    private val repetitionCreatorStateProvider = RepetitionCreatorStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState
    )

    private val repetitionCreatorState: RepetitionStateCreator.State =
        initialRepetitionCreatorState ?: repetitionCreatorStateProvider.load()

    private val presetDialogStateProvider = PresetDialogStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        key = "RepetitionSetting Preset State"
    )

    private val presetDialogState: PresetDialogState =
        initialPresetDialogState ?: presetDialogStateProvider.load()

    private val repetitionSettings = RepetitionSettings(
        AppDiScope.get().globalState
    )

    private val repetitionStateCreator = RepetitionStateCreator(
        repetitionCreatorState,
        AppDiScope.get().globalState
    )

    val presetController = RepetitionSettingsPresetController(
        repetitionSettings,
        presetDialogState,
        AppDiScope.get().globalState,
        AppDiScope.get().longTermStateSaver,
        presetDialogStateProvider
    )

    val presetViewModel = RepetitionSettingsPresetViewModel(
        presetDialogState,
        AppDiScope.get().globalState
    )

    val controller = RepetitionSettingsController(
        repetitionSettings,
        repetitionStateCreator,
        AppDiScope.get().globalState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        repetitionCreatorStateProvider
    )

    val viewModel = RepetitionSettingsViewModel(
        repetitionStateCreator,
        AppDiScope.get().globalState
    )

    companion object : DiScopeManager<RepetitionSettingsDiScope>() {
        fun create(
            initialRepetitionCreatorState: RepetitionStateCreator.State,
            initialPresetDialogState: PresetDialogState
        ) = RepetitionSettingsDiScope(
            initialRepetitionCreatorState,
            initialPresetDialogState
        )

        fun shareRepetitionSettings(): RepetitionSettings {
            return diScope?.repetitionSettings ?: error("RepetitionSettingsDiScope is not opened")
        }

        override fun recreateDiScope() = RepetitionSettingsDiScope()

        override fun onCloseDiScope(diScope: RepetitionSettingsDiScope) {
            diScope.controller.dispose()
            diScope.presetController.dispose()
        }
    }
}