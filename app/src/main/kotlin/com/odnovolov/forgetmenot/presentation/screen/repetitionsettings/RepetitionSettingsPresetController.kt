package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings

import com.odnovolov.forgetmenot.domain.checkRepetitionSettingName
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.customview.preset.DialogPurpose.*
import com.odnovolov.forgetmenot.presentation.common.customview.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.common.customview.preset.SkeletalPresetController

class RepetitionSettingsPresetController(
    private val repetitionSettings: RepetitionSettings,
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
        repetitionSettings.setCurrentRepetitionSetting(repetitionSettingId = id!!)
    }

    override fun getPresetName(id: Long): String {
        return globalState.sharedRepetitionSettings.first { it.id == id }.name
    }

    override fun onDeletePresetButtonClicked(id: Long) {
        repetitionSettings.deleteSharedRepetitionSetting(repetitionSettingId = id)
    }

    override fun onPresetNamePositiveDialogButtonClicked() {
        val newPresetName: String = presetDialogState.typedPresetName
        if (checkRepetitionSettingName(newPresetName, globalState) != NameCheckResult.Ok) return
        when (val purpose = presetDialogState.purpose) {
            ToMakeIndividualPresetAsShared -> {
                val repetitionSetting = globalState.currentRepetitionSetting
                repetitionSettings.renameRepetitionSetting(repetitionSetting, newPresetName)
            }
            ToCreateNewSharedPreset -> {
                repetitionSettings.createNewSharedRepetitionSetting(newPresetName)
            }
            is ToRenameSharedPreset -> {
                val repetitionSetting = globalState.sharedRepetitionSettings
                    .first { it.id == purpose.id }
                repetitionSettings.renameRepetitionSetting(repetitionSetting, newPresetName)
            }
        }
    }
}