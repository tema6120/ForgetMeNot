package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings

import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.checkRepetitionSettingName
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.entity.RepetitionSetting
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionStateCreator
import com.odnovolov.forgetmenot.domain.isIndividual
import com.odnovolov.forgetmenot.presentation.common.preset.Preset
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import com.odnovolov.forgetmenot.presentation.common.entity.NamePresetDialogStatus
import com.soywiz.klock.DateTimeSpan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import org.koin.java.KoinJavaComponent.getKoin

class RepetitionSettingsViewModel(
    screenState: RepetitionSettingsScreenState,
    private val repetitionStateCreator: RepetitionStateCreator,
    private val globalState: GlobalState
) : ViewModel() {
    private val currentRepetitionSetting: Flow<RepetitionSetting> = globalState
        .flowOf(GlobalState::currentRepetitionSetting)
        .share()

    val matchingCardsNumber: Flow<Int> = currentRepetitionSetting.flatMapLatest { it.asFlow() }
        .map { repetitionStateCreator.getCurrentMatchingCardsNumber() }

    val repetitionSetting: Flow<RepetitionSetting> = currentRepetitionSetting
        .flatMapLatest { repetitionSetting: RepetitionSetting ->
            repetitionSetting.flowOf(RepetitionSetting::name)
                .map { repetitionSetting }
        }

    val isSavePresetButtonEnabled: Flow<Boolean> =
        currentRepetitionSetting.map { it.isIndividual() }

    val availablePresets: Flow<List<Preset>> = combine(
        currentRepetitionSetting,
        globalState.flowOf(GlobalState::sharedRepetitionSettings)
    ) { currentRepetitionSetting: RepetitionSetting,
        sharedRepetitionSettings: Collection<RepetitionSetting>
        ->
        (sharedRepetitionSettings + currentRepetitionSetting + RepetitionSetting.Default)
            .distinctBy { it.id }
    }
        .flatMapLatest { repetitionSettings: List<RepetitionSetting> ->
            val repetitionSettingNameFlows: List<Flow<String>> = repetitionSettings
                .map { it.flowOf(RepetitionSetting::name) }
            combine(repetitionSettingNameFlows) {
                repetitionSettings
                    .map { repetitionSetting: RepetitionSetting ->
                        with(repetitionSetting) {
                            Preset(
                                id = id,
                                name = name,
                                isSelected = id == globalState.currentRepetitionSetting.id
                            )
                        }
                    }
                    .sortedWith(compareBy({ it.name }, { it.id }))
            }
        }

    val isNamePresetDialogVisible: Flow<Boolean> =
        screenState.flowOf(RepetitionSettingsScreenState::namePresetDialogStatus)
            .map { it != NamePresetDialogStatus.Invisible }

    val namePresetInputCheckResult: Flow<NameCheckResult> =
        screenState.flowOf(RepetitionSettingsScreenState::typedPresetName)
            .map { typedPresetName: String ->
                checkRepetitionSettingName(typedPresetName, globalState)
            }

    val isAvailableForExerciseGroupChecked: Flow<Boolean> = currentRepetitionSetting
        .flatMapLatest { repetitionSetting: RepetitionSetting ->
            repetitionSetting.flowOf(RepetitionSetting::isAvailableForExerciseCardsIncluded)
        }

    val isAwaitingGroupChecked: Flow<Boolean> = currentRepetitionSetting
        .flatMapLatest { repetitionSetting: RepetitionSetting ->
            repetitionSetting.flowOf(RepetitionSetting::isAwaitingCardsIncluded)
        }

    val isLearnedGroupChecked: Flow<Boolean> = currentRepetitionSetting
        .flatMapLatest { repetitionSetting: RepetitionSetting ->
            repetitionSetting.flowOf(RepetitionSetting::isLearnedCardsIncluded)
        }

    val availableLevelOfKnowledgeRange: IntRange = run {
        val maxLokFromCards: Int = globalState.decks
            .flatMap { deck -> deck.cards }
            .map { card -> card.levelOfKnowledge }
            .max() ?: 0
        val maxLokFromSharedIntervals: Int = globalState.sharedIntervalSchemes
            .flatMap { intervalScheme -> intervalScheme.intervals }
            .map { interval -> interval.targetLevelOfKnowledge }
            .max() ?: 0
        val maxLokFromDeckIntervals: Int = globalState.decks
            .flatMap { deck ->
                deck.exercisePreference.intervalScheme?.intervals ?: emptyList<Interval>()
            }
            .map { interval -> interval.targetLevelOfKnowledge }
            .max() ?: 0
        val maxLokFromSharedRepetitionSettings: Int = globalState.sharedRepetitionSettings
            .map { repetitionSetting -> repetitionSetting.levelOfKnowledgeRange.last }
            .max() ?: 0
        val maxLokFromCurrentRepetitionSetting: Int =
            globalState.currentRepetitionSetting.levelOfKnowledgeRange.last
        val maxLevelOfKnowledge: Int = arrayOf(
            maxLokFromCards,
            maxLokFromSharedIntervals,
            maxLokFromDeckIntervals,
            maxLokFromSharedRepetitionSettings,
            maxLokFromCurrentRepetitionSetting
        ).max()!!
        0..maxLevelOfKnowledge
    }

    val selectedLevelOfKnowledgeRange: Flow<IntRange> =
        currentRepetitionSetting.map { it.levelOfKnowledgeRange }

    val lastAnswerFromTimeAgo: Flow<DisplayedInterval?> = currentRepetitionSetting
        .flatMapLatest { repetitionSetting: RepetitionSetting ->
            repetitionSetting.flowOf(RepetitionSetting::lastAnswerFromTimeAgo)
        }
        .map { dateTimeSpan: DateTimeSpan? ->
            dateTimeSpan?.let(DisplayedInterval.Companion::fromDateTimeSpan)
        }

    val lastAnswerToTimeAgo: Flow<DisplayedInterval?> = currentRepetitionSetting
        .flatMapLatest { repetitionSetting: RepetitionSetting ->
            repetitionSetting.flowOf(RepetitionSetting::lastAnswerToTimeAgo)
        }
        .map { dateTimeSpan: DateTimeSpan? ->
            dateTimeSpan?.let(DisplayedInterval.Companion::fromDateTimeSpan)
        }

    val numberOfLaps: Flow<Int> = currentRepetitionSetting
        .flatMapLatest { repetitionSetting: RepetitionSetting ->
            repetitionSetting.flowOf(RepetitionSetting::numberOfLaps)
        }

    override fun onCleared() {
        getKoin().getScope(REPETITION_SETTINGS_SCOPE_ID).close()
    }
}