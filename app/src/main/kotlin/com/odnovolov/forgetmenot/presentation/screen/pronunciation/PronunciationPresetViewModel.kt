package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.common.customview.preset.Preset
import com.odnovolov.forgetmenot.presentation.common.customview.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.common.customview.preset.SkeletalPresetViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class PronunciationPresetViewModel(
    private val deckSettingsState: DeckSettings.State,
    presetDialogState: PresetDialogState,
    private val globalState: GlobalState
) : SkeletalPresetViewModel() {
    private val currentPronunciation: Flow<Pronunciation>
        get() = deckSettingsState.deck
            .flowOf(Deck::exercisePreference)
            .flatMapLatest { it.flowOf(ExercisePreference::pronunciation) }

    override val availablePresets: Flow<List<Preset>> = combine(
        currentPronunciation,
        globalState.flowOf(GlobalState::sharedPronunciations)
    ) { currentPronunciation: Pronunciation,
        sharedPronunciations: Collection<Pronunciation>
        ->
        (sharedPronunciations + currentPronunciation + Pronunciation.Default)
            .distinctBy { it.id }
    }
        .flatMapLatest { pronunciations: List<Pronunciation> ->
            val pronunciationNameFlows: List<Flow<String>> = pronunciations
                .map { it.flowOf(Pronunciation::name) }
            combine(pronunciationNameFlows) {
                val currentPronunciation = deckSettingsState.deck.exercisePreference.pronunciation
                pronunciations
                    .map { pronunciation: Pronunciation ->
                        with(pronunciation) {
                            Preset(
                                id = id,
                                name = name,
                                isSelected = id == currentPronunciation.id
                            )
                        }
                    }
                    .sortedWith(compareBy({ it.name }, { it.id }))
            }
        }

    override val presetInputCheckResult: Flow<NameCheckResult> = presetDialogState
        .flowOf(PresetDialogState::typedPresetName)
        .map { typedPresetName: String -> checkPronunciationName(typedPresetName, globalState) }

    override val deckNamesThatUsePreset: Flow<List<String>> = presetDialogState
        .flowOf(PresetDialogState::idToDelete)
        .map { sharedPronunciationIdToDelete ->
            globalState.decks
                .filter { deck: Deck ->
                    deck.exercisePreference.pronunciation.id == sharedPronunciationIdToDelete
                }
                .map { deck: Deck -> deck.name }
        }
}