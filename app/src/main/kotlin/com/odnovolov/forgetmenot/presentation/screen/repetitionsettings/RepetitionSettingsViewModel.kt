package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.entity.RepetitionSetting
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionStateCreator
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import com.soywiz.klock.DateTimeSpan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class RepetitionSettingsViewModel(
    private val repetitionStateCreator: RepetitionStateCreator,
    private val globalState: GlobalState
) {
    private val currentRepetitionSetting: Flow<RepetitionSetting> = globalState
        .flowOf(GlobalState::currentRepetitionSetting)
        .share()

    val matchingCardsNumber: Flow<Int> = currentRepetitionSetting.flatMapLatest { it.asFlow() }
        .map { repetitionStateCreator.getCurrentMatchingCardsNumber() }

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
            .maxOrNull() ?: 0
        val maxLokFromSharedIntervals: Int = globalState.sharedIntervalSchemes
            .flatMap { intervalScheme -> intervalScheme.intervals }
            .map { interval -> interval.levelOfKnowledge }
            .maxOrNull() ?: 0
        val maxLokFromDeckIntervals: Int = globalState.decks
            .flatMap { deck ->
                deck.exercisePreference.intervalScheme?.intervals ?: emptyList<Interval>()
            }
            .map { interval -> interval.levelOfKnowledge }
            .maxOrNull() ?: 0
        val maxLokFromSharedRepetitionSettings: Int = globalState.sharedRepetitionSettings
            .map { repetitionSetting -> repetitionSetting.levelOfKnowledgeRange.last }
            .maxOrNull() ?: 0
        val maxLokFromCurrentRepetitionSetting: Int =
            globalState.currentRepetitionSetting.levelOfKnowledgeRange.last
        val maxLevelOfKnowledge: Int = arrayOf(
            maxLokFromCards,
            maxLokFromSharedIntervals,
            maxLokFromDeckIntervals,
            maxLokFromSharedRepetitionSettings,
            maxLokFromCurrentRepetitionSetting
        ).maxOrNull()!!
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
}