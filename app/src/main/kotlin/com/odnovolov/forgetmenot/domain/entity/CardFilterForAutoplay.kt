package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import com.soywiz.klock.DateTimeSpan

class CardFilterForAutoplay(
    override val id: Long,
    isAvailableForExerciseCardsIncluded: Boolean,
    isAwaitingCardsIncluded: Boolean,
    isLearnedCardsIncluded: Boolean,
    gradeRange: IntRange,
    lastTestedFromTimeAgo: DateTimeSpan?,
    lastTestedToTimeAgo: DateTimeSpan?
) : FlowMakerWithRegistry<CardFilterForAutoplay>() {
    var isAvailableForExerciseCardsIncluded: Boolean by flowMaker(isAvailableForExerciseCardsIncluded)
    var isAwaitingCardsIncluded: Boolean by flowMaker(isAwaitingCardsIncluded)
    var isLearnedCardsIncluded: Boolean by flowMaker(isLearnedCardsIncluded)
    var gradeRange: IntRange by flowMaker(gradeRange)
    var lastTestedFromTimeAgo: DateTimeSpan? by flowMaker(lastTestedFromTimeAgo) // null means zero time
    var lastTestedToTimeAgo: DateTimeSpan? by flowMaker(lastTestedToTimeAgo) // null means now

    override fun copy() = CardFilterForAutoplay(
        id,
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
            CardFilterForAutoplay(
                id = 0L,
                isAvailableForExerciseCardsIncluded = false,
                isAwaitingCardsIncluded = true,
                isLearnedCardsIncluded = false,
                gradeRange = 0..maxGrade,
                lastTestedFromTimeAgo = null,
                lastTestedToTimeAgo = null
            )
        }

        val IncludeAll by lazy {
            CardFilterForAutoplay(
                id = -2L,
                isAvailableForExerciseCardsIncluded = true,
                isAwaitingCardsIncluded = true,
                isLearnedCardsIncluded = true,
                gradeRange = 0..Int.MAX_VALUE,
                lastTestedFromTimeAgo = null,
                lastTestedToTimeAgo = null
            )
        }
    }
}