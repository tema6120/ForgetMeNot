package com.odnovolov.forgetmenot.presentation.screen.speakplan

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.entity.checkSpeakPlanName
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.decksettings.SpeakPlanSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.customview.preset.DialogPurpose.*
import com.odnovolov.forgetmenot.presentation.common.customview.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.common.customview.preset.SkeletalPresetController
import com.odnovolov.forgetmenot.presentation.common.customview.preset.SkeletalPresetController.Command.ShowRemovePresetDialog

class SpeakPlanPresetController(
    private val deckSettingsState: DeckSettings.State,
    private val speakPlanSettings: SpeakPlanSettings,
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
        speakPlanSettings.setSpeakPlan(speakPlanId = id!!)
    }

    override fun getPresetName(id: Long): String {
        return globalState.sharedSpeakPlans.first { it.id == id }.name
    }

    override fun onDeletePresetButtonClicked(id: Long) {
        val isPresetInUse: Boolean = globalState.decks
            .any { deck: Deck -> deck.exercisePreference.speakPlan.id == id }
        if (isPresetInUse) {
            presetDialogState.idToDelete = id
            sendCommand(ShowRemovePresetDialog)
        } else {
            speakPlanSettings.deleteSharedSpeakPlan(speakPlanId = id)
        }
    }

    override fun onPresetNamePositiveDialogButtonClicked() {
        val newPresetName: String = presetDialogState.typedPresetName
        if (checkSpeakPlanName(newPresetName, globalState) != NameCheckResult.Ok) return
        when (val purpose = presetDialogState.purpose) {
            ToMakeIndividualPresetAsShared -> {
                val speakPlan = deckSettingsState.deck.exercisePreference.speakPlan
                speakPlanSettings.renameSpeakPlan(speakPlan, newPresetName)
            }
            ToCreateNewSharedPreset -> {
                speakPlanSettings.createNewSharedSpeakPlan(newPresetName)
            }
            is ToRenameSharedPreset -> {
                val speakPlan = globalState.sharedSpeakPlans.first { it.id == purpose.id }
                speakPlanSettings.renameSpeakPlan(speakPlan, newPresetName)
            }
        }
    }

    override fun onRemovePresetPositiveDialogButtonClicked() {
        presetDialogState.idToDelete?.let { id: Long ->
            speakPlanSettings.deleteSharedSpeakPlan(speakPlanId = id)
        }
        presetDialogState.idToDelete = null
    }
}