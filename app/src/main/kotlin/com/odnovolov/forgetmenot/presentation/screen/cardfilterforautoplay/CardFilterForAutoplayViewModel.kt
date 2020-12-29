package com.odnovolov.forgetmenot.presentation.screen.cardfilterforautoplay

import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.CardFilterForAutoplay
import com.odnovolov.forgetmenot.domain.interactor.autoplay.PlayerStateCreator
import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval
import com.soywiz.klock.DateTimeSpan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class CardFilterForAutoplayViewModel(
    private val playerStateCreator: PlayerStateCreator,
    private val globalState: GlobalState
) {
    private val cardFilter get() = globalState.cardFilterForAutoplay

    val matchingCardsNumber: Flow<Int> = cardFilter.asFlow()
        .map { playerStateCreator.getCurrentMatchingCardsNumber() }
        .distinctUntilChanged()

    val isAvailableForExerciseCheckboxChecked: Flow<Boolean> =
        cardFilter.flowOf(CardFilterForAutoplay::isAvailableForExerciseCardsIncluded)

    val isAwaitingCheckboxChecked: Flow<Boolean> =
        cardFilter.flowOf(CardFilterForAutoplay::isAwaitingCardsIncluded)

    val isLearnedCheckboxChecked: Flow<Boolean> =
        cardFilter.flowOf(CardFilterForAutoplay::isLearnedCardsIncluded)

    val availableGradeRange: IntRange = run {
        val maxGradeFromCards: Int = playerStateCreator.state.decks
            .flatMap { deck -> deck.cards }
            .map { card -> card.grade }
            .maxOrNull() ?: 0
        val maxGradeFromSavedFilter: Int = cardFilter.gradeRange.last
        val maxGrade: Int = maxOf(maxGradeFromCards, maxGradeFromSavedFilter, 6)
        0..maxGrade
    }

    val selectedGradeRange: Flow<IntRange> = cardFilter.flowOf(CardFilterForAutoplay::gradeRange)

    val lastTestedFromTimeAgo: Flow<DisplayedInterval?> =
        cardFilter.flowOf(CardFilterForAutoplay::lastTestedFromTimeAgo)
            .map { dateTimeSpan: DateTimeSpan? ->
                dateTimeSpan?.let(DisplayedInterval.Companion::fromDateTimeSpan)
            }

    val lastTestedToTimeAgo: Flow<DisplayedInterval?> =
        cardFilter.flowOf(CardFilterForAutoplay::lastTestedToTimeAgo)
            .map { dateTimeSpan: DateTimeSpan? ->
                dateTimeSpan?.let(DisplayedInterval.Companion::fromDateTimeSpan)
            }
}