package com.odnovolov.forgetmenot.presentation.screen.speakplan

import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.common.customview.preset.Preset
import com.odnovolov.forgetmenot.presentation.common.customview.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.common.customview.preset.SkeletalPresetViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class SpeakPlanPresetViewModel(
    private val deckSettingsState: DeckSettings.State,
    presetDialogState: PresetDialogState,
    private val globalState: GlobalState
) : SkeletalPresetViewModel() {
    private val currentSpeakPlan: Flow<SpeakPlan>
        get() = deckSettingsState.deck
            .flowOf(Deck::exercisePreference)
            .flatMapLatest { it.flowOf(ExercisePreference::speakPlan) }

    override val availablePresets: Flow<List<Preset>> = combine(
        currentSpeakPlan,
        globalState.flowOf(GlobalState::sharedSpeakPlans)
    ) { currentSpeakPlan: SpeakPlan,
        sharedSpeakPlans: Collection<SpeakPlan>
        ->
        (sharedSpeakPlans + currentSpeakPlan + SpeakPlan.Default)
            .distinctBy { it.id }
    }
        .flatMapLatest { speakPlans: List<SpeakPlan> ->
            val speakPlanNameFlows: List<Flow<String>> = speakPlans
                .map { it.flowOf(SpeakPlan::name) }
            combine(speakPlanNameFlows) {
                val currentSpeakPlan = deckSettingsState.deck.exercisePreference.speakPlan
                speakPlans
                    .map { speakPlan: SpeakPlan ->
                        Preset(
                            id = speakPlan.id,
                            name = speakPlan.name,
                            isSelected = speakPlan.id == currentSpeakPlan.id
                        )
                    }
                    .sortedWith(compareBy({ it.name }, { it.id }))
            }
        }

    override val presetInputCheckResult: Flow<NameCheckResult> = presetDialogState
        .flowOf(PresetDialogState::typedPresetName)
        .map { typedPresetName: String -> checkSpeakPlanName(typedPresetName, globalState) }
}