package com.odnovolov.forgetmenot.presentation.screen.intervals

import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.common.customview.preset.Preset
import com.odnovolov.forgetmenot.presentation.common.customview.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.common.customview.preset.SkeletalPresetViewModel
import kotlinx.coroutines.flow.*

class IntervalsPresetViewModel(
    private val deckSettingsState: DeckSettings.State,
    presetDialogState: PresetDialogState,
    private val globalState: GlobalState
) : SkeletalPresetViewModel() {
    private val currentIntervalScheme: Flow<IntervalScheme?>
        get() = deckSettingsState.deck.flowOf(Deck::exercisePreference)
            .flatMapLatest { exercisePreference: ExercisePreference ->
                exercisePreference.flowOf(ExercisePreference::intervalScheme)
            }

    override val availablePresets: Flow<List<Preset>> = combine(
        currentIntervalScheme,
        globalState.flowOf(GlobalState::sharedIntervalSchemes)
    ) { currentIntervalScheme: IntervalScheme?,
        sharedIntervalSchemes: Collection<IntervalScheme>
        ->
        (sharedIntervalSchemes + IntervalScheme.Default + currentIntervalScheme + listOf(null))
            .distinctBy { it?.id }
    }
        .flatMapLatest { intervalSchemes: List<IntervalScheme?> ->
            val intervalSchemeNameFlows: List<Flow<String?>> = intervalSchemes
                .map { it?.flowOf(IntervalScheme::name) ?: flowOf(null) }
            combine(intervalSchemeNameFlows) {
                val currentIntervalScheme: IntervalScheme? =
                    deckSettingsState.deck.exercisePreference.intervalScheme
                intervalSchemes
                    .map { intervalScheme: IntervalScheme? ->
                        if (intervalScheme == null) {
                            Preset(
                                id = null,
                                name = "",
                                isSelected = currentIntervalScheme == null
                            )
                        } else {
                            Preset(
                                id = intervalScheme.id,
                                name = intervalScheme.name,
                                isSelected = currentIntervalScheme
                                    ?.let { it.id == intervalScheme.id }
                                    ?: false
                            )
                        }
                    }
                    .sortedWith(compareBy({ it.name }, { it.id }))
            }
        }

    override val presetInputCheckResult: Flow<NameCheckResult> = presetDialogState
        .flowOf(PresetDialogState::typedPresetName)
        .map { typedPresetName: String -> checkIntervalSchemeName(typedPresetName, globalState) }

    override val deckNamesThatUsePreset: Flow<List<String>> = presetDialogState
        .flowOf(PresetDialogState::idToDelete)
        .map { sharedIntervalSchemeIdToDelete ->
            globalState.decks
                .filter { deck: Deck ->
                    deck.exercisePreference.intervalScheme?.id == sharedIntervalSchemeIdToDelete
                }
                .map { deck: Deck -> deck.name }
        }
}