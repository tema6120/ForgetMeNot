package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import com.odnovolov.forgetmenot.domain.checkPronunciationName
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.entity.Pronunciation
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.decksettings.PronunciationSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.common.preset.DialogPurpose.*
import com.odnovolov.forgetmenot.presentation.common.preset.SkeletalPresetController

class PronunciationPresetController(
    private val deckSettingsState: DeckSettings.State,
    private val pronunciationSettings: PronunciationSettings,
    private val presetDialogState: PresetDialogState,
    private val globalState: GlobalState,
    private val longTermStateSaver: LongTermStateSaver,
    dialogStateProvider: ShortTermStateProvider<PresetDialogState>
) : SkeletalPresetController(presetDialogState, dialogStateProvider) {

    override fun onSetPresetButtonClicked(id: Long?) {
        pronunciationSettings.setPronunciation(pronunciationId = id!!)
        longTermStateSaver.saveStateByRegistry()
    }

    override fun getPresetName(id: Long): String {
        return globalState.sharedPronunciations.first { it.id == id }.name
    }

    override fun onDeletePresetButtonClicked(id: Long) {
        pronunciationSettings.deleteSharedPronunciation(pronunciationId = id)
        longTermStateSaver.saveStateByRegistry()
    }

    override fun onPresetNamePositiveDialogButtonClicked() {
        val newPresetName: String = presetDialogState.typedPresetName
        if (checkPronunciationName(newPresetName, globalState) != NameCheckResult.Ok) return
        when (val purpose = presetDialogState.purpose) {
            ToMakeIndividualPresetAsShared -> {
                val pronunciation = deckSettingsState.deck.exercisePreference.pronunciation
                pronunciationSettings.renamePronunciation(pronunciation, newPresetName)
            }
            ToCreateNewSharedPreset -> {
                pronunciationSettings.createNewSharedPronunciation(newPresetName)
            }
            is ToRenameSharedPreset -> {
                globalState.sharedPronunciations
                    .find { it.id == purpose.id }
                    ?.let { pronunciation: Pronunciation ->
                        pronunciationSettings.renamePronunciation(pronunciation, newPresetName)
                    }
            }
        }
        longTermStateSaver.saveStateByRegistry()
    }
}