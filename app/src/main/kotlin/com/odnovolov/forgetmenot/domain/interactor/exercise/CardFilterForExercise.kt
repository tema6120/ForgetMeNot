package com.odnovolov.forgetmenot.domain.interactor.exercise

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import com.odnovolov.forgetmenot.domain.entity.CardFilterLastTested
import com.soywiz.klock.DateTimeSpan

class CardFilterForExercise (
    limit: Int,
    gradeRange: IntRange,
    lastTestedFromTimeAgo: DateTimeSpan?,
    lastTestedToTimeAgo: DateTimeSpan?
) : FlowMakerWithRegistry<CardFilterForExercise>(), CardFilterLastTested {
    var limit: Int by flowMaker(limit) // -1 means no limit
    var gradeRange: IntRange by flowMaker(gradeRange)
    override var lastTestedFromTimeAgo: DateTimeSpan? by flowMaker(lastTestedFromTimeAgo)
    override var lastTestedToTimeAgo: DateTimeSpan? by flowMaker(lastTestedToTimeAgo)

    override fun copy() = CardFilterForExercise(
        limit,
        gradeRange,
        lastTestedFromTimeAgo,
        lastTestedToTimeAgo
    )

    companion object {
        val Default by lazy {
            CardFilterForExercise(
                limit = 100,
                gradeRange = 0..6,
                lastTestedFromTimeAgo = null,
                lastTestedToTimeAgo = null
            )
        }

        const val CARD_FILTER_NO_LIMIT = -1
    }
}