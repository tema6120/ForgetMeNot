package com.odnovolov.forgetmenot.presentation.screen.cardfilterforexercise

import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.exercise.CardFilterForExercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseStateCreatorWithFiltering
import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval
import com.soywiz.klock.DateTimeSpan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class CardFilterForExerciseViewModel(
    private val exerciseStateCreator: ExerciseStateCreatorWithFiltering,
    private val globalState: GlobalState
) {
    private val cardFilter get() = globalState.cardFilterForExercise

    val matchingCardsNumber: Flow<Int> = cardFilter.asFlow()
        .map { exerciseStateCreator.getCurrentMatchingCardsNumber() }
        .distinctUntilChanged()

    val limit: Flow<Int> = cardFilter.flowOf(CardFilterForExercise::limit)

    val availableGradeRange: IntRange = run {
        val maxGradeFromCards: Int = exerciseStateCreator.state.decks
            .flatMap { deck -> deck.cards }
            .map { card -> card.grade }
            .maxOrNull() ?: 0
        val maxGradeFromSavedFilter: Int = cardFilter.gradeRange.last
        val maxGrade: Int = maxOf(maxGradeFromCards, maxGradeFromSavedFilter, 6)
        0..maxGrade
    }

    val selectedGradeRange: Flow<IntRange> = cardFilter.flowOf(CardFilterForExercise::gradeRange)

    val lastTestedFromTimeAgo: Flow<DisplayedInterval?> =
        cardFilter.flowOf(CardFilterForExercise::lastTestedFromTimeAgo)
            .map { dateTimeSpan: DateTimeSpan? ->
                dateTimeSpan?.let(DisplayedInterval.Companion::fromDateTimeSpan)
            }

    val lastTestedToTimeAgo: Flow<DisplayedInterval?> =
        cardFilter.flowOf(CardFilterForExercise::lastTestedToTimeAgo)
            .map { dateTimeSpan: DateTimeSpan? ->
                dateTimeSpan?.let(DisplayedInterval.Companion::fromDateTimeSpan)
            }
}