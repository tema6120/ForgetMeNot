package com.odnovolov.forgetmenot.presentation.screen.intervals

import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.presentation.common.preset.Preset
import com.odnovolov.forgetmenot.presentation.common.entity.NamePresetDialogStatus
import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.checkIntervalSchemeName
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.isIndividual
import kotlinx.coroutines.flow.*
import org.koin.core.KoinComponent

class IntervalsViewModel(
    private val deckSettingsState: DeckSettings.State,
    intervalsScreenState: IntervalsScreenState,
    private val globalState: GlobalState
) : ViewModel(), KoinComponent {
    private val currentIntervalScheme: Flow<IntervalScheme?> = deckSettingsState.deck
        .flowOf(Deck::exercisePreference)
        .flatMapLatest { exercisePreference: ExercisePreference ->
            exercisePreference.flowOf(ExercisePreference::intervalScheme)
        }
        .share()

    val intervalScheme: Flow<IntervalScheme?> =
        currentIntervalScheme.flatMapLatest { intervalScheme: IntervalScheme? ->
            intervalScheme?.asFlow() ?: flowOf(null)
        }

    val availableIntervalSchemes: Flow<List<Preset>> = combine(
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

    val isSaveIntervalSchemeButtonEnabled: Flow<Boolean> =
        intervalScheme.map { intervalScheme: IntervalScheme? ->
            intervalScheme?.isIndividual() ?: false
        }

    val isNamePresetDialogVisible: Flow<Boolean> =
        intervalsScreenState.flowOf(IntervalsScreenState::namePresetDialogStatus)
            .map { namePresetDialogStatus: NamePresetDialogStatus ->
                namePresetDialogStatus != NamePresetDialogStatus.Invisible
            }

    val namePresetInputCheckResult: Flow<NameCheckResult> =
        intervalsScreenState.flowOf(IntervalsScreenState::typedPresetName)
            .map { typedPresetName: String ->
                checkIntervalSchemeName(typedPresetName, globalState)
            }

    val intervals: Flow<List<Interval>> = intervalScheme
        .flatMapLatest {
            it?.let { intervalScheme: IntervalScheme ->
                intervalScheme.flowOf(IntervalScheme::intervals)
                    .flatMapLatest { intervals: List<Interval> ->
                        val intervalsFlows: List<Flow<Interval>> =
                            intervals.map { it.asFlow() }
                        combine(intervalsFlows) { intervalScheme.intervals.copy() }
                    }
            } ?: flowOf(emptyList<Interval>())
        }

    val isRemoveIntervalButtonEnabled: Flow<Boolean> = intervals.map { it.size > 1 }

    override fun onCleared() {
        getKoin().getScope(INTERVALS_SCOPE_ID).close()
    }
}