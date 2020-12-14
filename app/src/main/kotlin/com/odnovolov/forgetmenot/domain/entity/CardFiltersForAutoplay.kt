package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import com.soywiz.klock.DateTimeSpan

class CardFiltersForAutoplay(
    isAvailableForExerciseCardsIncluded: Boolean,
    isAwaitingCardsIncluded: Boolean,
    isLearnedCardsIncluded: Boolean,
    gradeRange: IntRange,
    lastTestedFromTimeAgo: DateTimeSpan?,
    lastTestedToTimeAgo: DateTimeSpan?
) : FlowMakerWithRegistry<CardFiltersForAutoplay>() {
    var isAvailableForExerciseCardsIncluded: Boolean by flowMaker(isAvailableForExerciseCardsIncluded)
    var isAwaitingCardsIncluded: Boolean by flowMaker(isAwaitingCardsIncluded)
    var isLearnedCardsIncluded: Boolean by flowMaker(isLearnedCardsIncluded)
    var gradeRange: IntRange by flowMaker(gradeRange)
    var lastTestedFromTimeAgo: DateTimeSpan? by flowMaker(lastTestedFromTimeAgo) // null means zero time
    var lastTestedToTimeAgo: DateTimeSpan? by flowMaker(lastTestedToTimeAgo) // null means now

    override fun copy() = CardFiltersForAutoplay(
        isAvailableForExerciseCardsIncluded,
        isAwaitingCardsIncluded,
        isLearnedCardsIncluded,
        gradeRange,
        lastTestedFromTimeAgo,
        lastTestedToTimeAgo
    )

    companion object {
        val Default by lazy {
            val maxGrade: Int = IntervalScheme.Default.intervals.last().grade + 1
            CardFiltersForAutoplay(
                isAvailableForExerciseCardsIncluded = false,
                isAwaitingCardsIncluded = true,
                isLearnedCardsIncluded = false,
                gradeRange = 0..maxGrade,
                lastTestedFromTimeAgo = null,
                lastTestedToTimeAgo = null
            )
        }
    }
}