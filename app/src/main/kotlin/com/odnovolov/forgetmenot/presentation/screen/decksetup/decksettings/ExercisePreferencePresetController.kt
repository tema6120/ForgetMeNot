package com.odnovolov.forgetmenot.presentation.screen.decksetup.decksettings

import com.odnovolov.forgetmenot.domain.checkExercisePreferenceName
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.customview.preset.DialogPurpose.*
import com.odnovolov.forgetmenot.presentation.common.customview.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.common.customview.preset.SkeletalPresetController

class ExercisePreferencePresetController(
    private val deckSettings: DeckSettings,
    private val presetDialogState: PresetDialogState,
    private val globalState: GlobalState,
    presetDialogStateProvider: ShortTermStateProvider<PresetDialogState>,
    longTermStateSaver: LongTermStateSaver
) : SkeletalPresetController(
    presetDialogState,
    presetDialogStateProvider,
    longTermStateSaver
) {
    override fun onSetPresetButtonClicked(id: Long?) {
        deckSettings.setExercisePreference(exercisePreferenceId = id!!)
    }

    override fun getPresetName(id: Long): String {
        return globalState.sharedExercisePreferences.first { it.id == id }.name
    }

    override fun onDeletePresetButtonClicked(id: Long) {
        deckSettings.deleteSharedExercisePreference(exercisePreferenceId = id)
    }

    override fun onPresetNamePositiveDialogButtonClicked() {
        val newPresetName: String = presetDialogState.typedPresetName
        if (checkExercisePreferenceName(newPresetName, globalState) != NameCheckResult.Ok) return
        when (val purpose = presetDialogState.purpose) {
            ToMakeIndividualPresetAsShared -> {
                val currentExercisePreference = deckSettings.state.deck.exercisePreference
                deckSettings.renameExercisePreference(currentExercisePreference, newPresetName)
            }
            ToCreateNewSharedPreset -> {
                deckSettings.createNewSharedExercisePreference(newPresetName)
            }
            is ToRenameSharedPreset -> {
                val exercisePreference = globalState.sharedExercisePreferences
                    .first { it.id == purpose.id }
                deckSettings.renameExercisePreference(exercisePreference, newPresetName)
            }
        }
    }
}