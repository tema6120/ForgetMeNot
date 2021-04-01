package com.odnovolov.forgetmenot.domain.interactor.autoplay

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import com.odnovolov.forgetmenot.domain.entity.CardFilterLastTested
import com.soywiz.klock.DateTimeSpan

class CardFilterForAutoplay(
    isAvailableForExerciseCardsIncluded: Boolean,
    isAwaitingCardsIncluded: Boolean,
    isLearnedCardsIncluded: Boolean,
    gradeRange: IntRange,
    lastTestedFromTimeAgo: DateTimeSpan?,
    lastTestedToTimeAgo: DateTimeSpan?
) : FlowMakerWithRegistry<CardFilterForAutoplay>(), CardFilterLastTested {
    var areCardsAvailableForExerciseIncluded: Boolean
            by flowMaker(isAvailableForExerciseCardsIncluded)
    var areAwaitingCardsIncluded: Boolean by flowMaker(isAwaitingCardsIncluded)
    var areLearnedCardsIncluded: Boolean by flowMaker(isLearnedCardsIncluded)
    var gradeRange: IntRange by flowMaker(gradeRange)
    override var lastTestedFromTimeAgo: DateTimeSpan? by flowMaker(lastTestedFromTimeAgo)
    override var lastTestedToTimeAgo: DateTimeSpan? by flowMaker(lastTestedToTimeAgo)

    override fun copy() = CardFilterForAutoplay(
        areCardsAvailableForExerciseIncluded,
        areAwaitingCardsIncluded,
        areLearnedCardsIncluded,
        gradeRange,
        lastTestedFromTimeAgo,
        lastTestedToTimeAgo
    )

    companion object {
        val Default by lazy {
            CardFilterForAutoplay(
                isAvailableForExerciseCardsIncluded = false,
                isAwaitingCardsIncluded = true,
                isLearnedCardsIncluded = false,
                gradeRange = 0..2,
                lastTestedFromTimeAgo = null,
                lastTestedToTimeAgo = null
            )
        }

        val IncludeAll by lazy {
            CardFilterForAutoplay(
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