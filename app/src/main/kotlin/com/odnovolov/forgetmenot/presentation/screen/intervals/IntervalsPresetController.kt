package com.odnovolov.forgetmenot.presentation.screen.intervals

import com.odnovolov.forgetmenot.domain.checkIntervalSchemeName
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.decksettings.IntervalsSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.preset.DialogPurpose.*
import com.odnovolov.forgetmenot.presentation.common.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.common.preset.SkeletalPresetController

class IntervalsPresetController(
    private val deckSettingsState: DeckSettings.State,
    private val intervalsSettings: IntervalsSettings,
    private val presetDialogState: PresetDialogState,
    private val globalState: GlobalState,
    longTermStateSaver: LongTermStateSaver,
    presetDialogStateProvider: ShortTermStateProvider<PresetDialogState>
) : SkeletalPresetController(
    presetDialogState,
    presetDialogStateProvider,
    longTermStateSaver
) {
    override fun onSetPresetButtonClicked(id: Long?) {
        intervalsSettings.setIntervalScheme(intervalSchemeId = id)
    }

    override fun getPresetName(id: Long): String {
        return globalState.sharedIntervalSchemes.first { it.id == id }.name
    }

    override fun onDeletePresetButtonClicked(id: Long) {
        intervalsSettings.deleteSharedIntervalScheme(intervalSchemeId = id)
    }

    override fun onPresetNamePositiveDialogButtonClicked() {
        val newPresetName: String = presetDialogState.typedPresetName
        if (checkIntervalSchemeName(newPresetName, globalState) != NameCheckResult.Ok) return
        when (val purpose = presetDialogState.purpose) {
            ToMakeIndividualPresetAsShared -> {
                val intervalScheme = deckSettingsState.deck.exercisePreference.intervalScheme!!
                intervalsSettings.renameIntervalScheme(intervalScheme, newPresetName)
            }
            ToCreateNewSharedPreset -> {
                intervalsSettings.createNewSharedIntervalScheme(newPresetName)
            }
            is ToRenameSharedPreset -> {
                val intervalScheme = globalState.sharedIntervalSchemes.first { it.id == purpose.id }
                intervalsSettings.renameIntervalScheme(intervalScheme, newPresetName)
            }
        }
    }
}