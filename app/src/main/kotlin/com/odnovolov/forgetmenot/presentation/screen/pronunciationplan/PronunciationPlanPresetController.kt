package com.odnovolov.forgetmenot.presentation.screen.pronunciationplan

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.entity.checkPronunciationPlanName
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.decksettings.PronunciationPlanSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.customview.preset.DialogPurpose.*
import com.odnovolov.forgetmenot.presentation.common.customview.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.common.customview.preset.SkeletalPresetController
import com.odnovolov.forgetmenot.presentation.common.customview.preset.SkeletalPresetController.Command.ShowRemovePresetDialog
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.help.HelpDiScope

class PronunciationPlanPresetController(
    private val deckSettingsState: DeckSettings.State,
    private val pronunciationPlanSettings: PronunciationPlanSettings,
    private val presetDialogState: PresetDialogState,
    private val globalState: GlobalState,
    private val navigator: Navigator,
    longTermStateSaver: LongTermStateSaver,
    presetDialogStateProvider: ShortTermStateProvider<PresetDialogState>
) : SkeletalPresetController(
    presetDialogState,
    presetDialogStateProvider,
    longTermStateSaver
) {
    override fun onSetPresetButtonClicked(id: Long?) {
        pronunciationPlanSettings.setPronunciationPlan(pronunciationPlanId = id!!)
    }

    override fun getPresetName(id: Long): String {
        return globalState.sharedPronunciationPlans.first { it.id == id }.name
    }

    override fun onDeletePresetButtonClicked(id: Long) {
        val isPresetInUse: Boolean = globalState.decks
            .any { deck: Deck -> deck.exercisePreference.pronunciationPlan.id == id }
        if (isPresetInUse) {
            presetDialogState.idToDelete = id
            sendCommand(ShowRemovePresetDialog)
        } else {
            pronunciationPlanSettings.deleteSharedPronunciationPlan(pronunciationPlanId = id)
        }
    }

    override fun onPresetNamePositiveDialogButtonClicked() {
        val newPresetName: String = presetDialogState.typedPresetName
        if (checkPronunciationPlanName(newPresetName, globalState) != NameCheckResult.Ok) return
        when (val purpose = presetDialogState.purpose) {
            ToMakeIndividualPresetAsShared -> {
                val pronunciationPlan = deckSettingsState.deck.exercisePreference.pronunciationPlan
                pronunciationPlanSettings.renamePronunciationPlan(pronunciationPlan, newPresetName)
            }
            ToCreateNewSharedPreset -> {
                pronunciationPlanSettings.createNewSharedPronunciationPlan(newPresetName)
            }
            is ToRenameSharedPreset -> {
                val pronunciationPlan =
                    globalState.sharedPronunciationPlans.first { it.id == purpose.id }
                pronunciationPlanSettings.renamePronunciationPlan(pronunciationPlan, newPresetName)
            }
        }
    }

    override fun onRemovePresetPositiveDialogButtonClicked() {
        presetDialogState.idToDelete?.let { id: Long ->
            pronunciationPlanSettings.deleteSharedPronunciationPlan(pronunciationPlanId = id)
        }
        presetDialogState.idToDelete = null
    }

    override fun onHelpButtonClicked() {
        navigator.navigateToHelpFromPronunciationPlan {
            HelpDiScope(HelpArticle.Presets)
        }
    }
}