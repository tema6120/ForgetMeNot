package com.odnovolov.forgetmenot.presentation.screen.decksettings

import com.odnovolov.forgetmenot.domain.checkExercisePreferenceName
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.ExercisePreference
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.common.customview.preset.Preset
import com.odnovolov.forgetmenot.presentation.common.customview.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.common.customview.preset.SkeletalPresetViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class ExercisePreferencePresetViewModel(
    presetDialogState: PresetDialogState,
    private val deckSettingsState: DeckSettings.State,
    private val globalState: GlobalState
) : SkeletalPresetViewModel() {
    override val availablePresets: Flow<List<Preset>> = combine(
        deckSettingsState.deck.flowOf(Deck::exercisePreference),
        globalState.flowOf(GlobalState::sharedExercisePreferences)
    ) { currentExercisePreference: ExercisePreference,
        sharedExercisePreferences: Collection<ExercisePreference>
        ->
        (sharedExercisePreferences + currentExercisePreference + ExercisePreference.Default)
            .distinctBy { it.id }
    }
        .flatMapLatest { exercisePreferences: List<ExercisePreference> ->
            val exercisePreferenceNameFlows: List<Flow<String>> = exercisePreferences
                .map { it.flowOf(ExercisePreference::name) }
            combine(exercisePreferenceNameFlows) {
                val currentExercisePreference = deckSettingsState.deck.exercisePreference
                exercisePreferences.map { exercisePreference: ExercisePreference ->
                    Preset(
                        id = exercisePreference.id,
                        name = exercisePreference.name,
                        isSelected = exercisePreference.id == currentExercisePreference.id
                    )
                }
                    .sortedWith(compareBy({ it.name }, { it.id }))
            }
        }

    override val presetInputCheckResult: Flow<NameCheckResult> = presetDialogState
        .flowOf(PresetDialogState::typedPresetName)
        .map { typedPresetName: String ->
            checkExercisePreferenceName(typedPresetName, globalState)
        }
}