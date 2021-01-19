package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import com.soywiz.klock.DateTime

class Card(
    override val id: Long,
    question: String,
    answer: String,
    lap: Int = 0,
    isLearned: Boolean = false,
    grade: Int = 0,
    lastTestedAt: DateTime? = null
) : FlowMakerWithRegistry<Card>() {
    var question: String by flowMaker(question)
    var answer: String by flowMaker(answer)
    var lap: Int by flowMaker(lap)
    var isLearned: Boolean by flowMaker(isLearned)
    var grade: Int by flowMaker(grade)
    var lastTestedAt: DateTime? by flowMaker(lastTestedAt)

    override fun copy() =
        Card(id, question, answer, lap, isLearned, grade, lastTestedAt)
}