package com.odnovolov.forgetmenot.presentation.screen.decksettings

import com.odnovolov.forgetmenot.domain.checkExercisePreferenceName
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.preset.DialogPurpose.*
import com.odnovolov.forgetmenot.presentation.common.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.common.preset.SkeletalPresetController

class ExercisePreferencePresetController(
    private val deckSettings: DeckSettings,
    private val presetDialogState: PresetDialogState,
    private val globalState: GlobalState,
    presetDialogStateProvider: ShortTermStateProvider<PresetDialogState>,
    private val longTermStateSaver: LongTermStateSaver
) : SkeletalPresetController(presetDialogState, presetDialogStateProvider) {

    override fun onSetPresetButtonClicked(id: Long?) {
        deckSettings.setExercisePreference(exercisePreferenceId = id!!)
        longTermStateSaver.saveStateByRegistry()
    }

    override fun getPresetName(id: Long): String {
        return globalState.sharedExercisePreferences.first { it.id == id }.name
    }

    override fun onDeletePresetButtonClicked(id: Long) {
        deckSettings.deleteSharedExercisePreference(exercisePreferenceId = id)
        longTermStateSaver.saveStateByRegistry()
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
                globalState.sharedExercisePreferences
                    .find { it.id == purpose.id }
                    ?.let { exercisePreference ->
                        deckSettings.renameExercisePreference(exercisePreference, newPresetName)
                    }
            }
        }
        longTermStateSaver.saveStateByRegistry()
    }
}