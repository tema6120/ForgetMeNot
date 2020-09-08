package com.odnovolov.forgetmenot.presentation.screen.pronunciationplan

import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.common.customview.preset.Preset
import com.odnovolov.forgetmenot.presentation.common.customview.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.common.customview.preset.SkeletalPresetViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class PronunciationPlanPresetViewModel(
    private val deckSettingsState: DeckSettings.State,
    presetDialogState: PresetDialogState,
    private val globalState: GlobalState
) : SkeletalPresetViewModel() {
    private val currentPronunciationPlan: Flow<PronunciationPlan>
        get() = deckSettingsState.deck
            .flowOf(Deck::exercisePreference)
            .flatMapLatest { it.flowOf(ExercisePreference::pronunciationPlan) }

    override val availablePresets: Flow<List<Preset>> = combine(
        currentPronunciationPlan,
        globalState.flowOf(GlobalState::sharedPronunciationPlans)
    ) { currentPronunciationPlan: PronunciationPlan,
        sharedPronunciationPlans: Collection<PronunciationPlan>
        ->
        (sharedPronunciationPlans + currentPronunciationPlan + PronunciationPlan.Default)
            .distinctBy { it.id }
    }
        .flatMapLatest { pronunciationPlans: List<PronunciationPlan> ->
            val pronunciationPlanNameFlows: List<Flow<String>> = pronunciationPlans
                .map { it.flowOf(PronunciationPlan::name) }
            combine(pronunciationPlanNameFlows) {
                val currentPronunciationPlan =
                    deckSettingsState.deck.exercisePreference.pronunciationPlan
                pronunciationPlans
                    .map { pronunciationPlan: PronunciationPlan ->
                        Preset(
                            id = pronunciationPlan.id,
                            name = pronunciationPlan.name,
                            isSelected = pronunciationPlan.id == currentPronunciationPlan.id
                        )
                    }
                    .sortedWith(compareBy({ it.name }, { it.id }))
            }
        }

    override val presetInputCheckResult: Flow<NameCheckResult> = presetDialogState
        .flowOf(PresetDialogState::typedPresetName)
        .map { typedPresetName: String -> checkPronunciationPlanName(typedPresetName, globalState) }

    override val deckNamesThatUsePreset: Flow<List<String>> = presetDialogState
        .flowOf(PresetDialogState::idToDelete)
        .map { sharedPronunciationPlanIdToDelete ->
            globalState.decks
                .filter { deck: Deck ->
                    deck.exercisePreference.pronunciationPlan.id == sharedPronunciationPlanIdToDelete
                }
                .map { deck: Deck -> deck.name }
        }
}