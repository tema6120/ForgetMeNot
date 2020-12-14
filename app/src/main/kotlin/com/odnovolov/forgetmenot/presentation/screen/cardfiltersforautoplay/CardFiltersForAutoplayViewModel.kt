package com.odnovolov.forgetmenot.presentation.screen.cardfiltersforautoplay

import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.CardFiltersForAutoplay
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionStateCreator
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import com.soywiz.klock.DateTimeSpan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CardFiltersForAutoplayViewModel(
    private val repetitionStateCreator: RepetitionStateCreator,
    private val globalState: GlobalState
) {
    private val cardFilters get() = globalState.cardFiltersForAutoplay

    val matchingCardsNumber: Flow<Int> = cardFilters.asFlow()
        .map { repetitionStateCreator.getCurrentMatchingCardsNumber() }

    val isAvailableForExerciseCheckboxChecked: Flow<Boolean> =
        cardFilters.flowOf(CardFiltersForAutoplay::isAvailableForExerciseCardsIncluded)

    val isAwaitingCheckboxChecked: Flow<Boolean> =
        cardFilters.flowOf(CardFiltersForAutoplay::isAwaitingCardsIncluded)

    val isLearnedCheckboxChecked: Flow<Boolean> =
        cardFilters.flowOf(CardFiltersForAutoplay::isLearnedCardsIncluded)

    val availableGradeRange: IntRange = run {
        val maxGradeFromCards: Int = globalState.decks
            .flatMap { deck -> deck.cards }
            .map { card -> card.grade }
            .maxOrNull() ?: 0
        val maxGradeFromSharedIntervals: Int = globalState.sharedIntervalSchemes
            .flatMap { intervalScheme -> intervalScheme.intervals }
            .map { interval -> interval.grade }
            .maxOrNull() ?: 0
        val maxGradeFromDeckIntervals: Int = globalState.decks
            .flatMap { deck ->
                deck.exercisePreference.intervalScheme?.intervals ?: emptyList()
            }
            .map { interval -> interval.grade }
            .maxOrNull() ?: 0
        val maxGradeFromCurrentRepetitionSetting: Int = cardFilters.gradeRange.last
        val maxGrade: Int = arrayOf(
            maxGradeFromCards,
            maxGradeFromSharedIntervals,
            maxGradeFromDeckIntervals,
            maxGradeFromCurrentRepetitionSetting
        ).maxOrNull()!!
        0..maxGrade
    }

    val selectedGradeRange: Flow<IntRange> = cardFilters.flowOf(CardFiltersForAutoplay::gradeRange)

    val lastTestedFromTimeAgo: Flow<DisplayedInterval?> =
        cardFilters.flowOf(CardFiltersForAutoplay::lastTestedFromTimeAgo)
            .map { dateTimeSpan: DateTimeSpan? ->
                dateTimeSpan?.let(DisplayedInterval.Companion::fromDateTimeSpan)
            }

    val lastTestedToTimeAgo: Flow<DisplayedInterval?> =
        cardFilters.flowOf(CardFiltersForAutoplay::lastTestedToTimeAgo)
            .map { dateTimeSpan: DateTimeSpan? ->
                dateTimeSpan?.let(DisplayedInterval.Companion::fromDateTimeSpan)
            }
}