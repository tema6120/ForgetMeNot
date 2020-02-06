package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.Copyable
import com.odnovolov.forgetmenot.domain.architecturecomponents.RegistrableFlowableState
import com.soywiz.klock.DateTime

class Card(
    override val id: Long,
    question: String,
    answer: String,
    lap: Int = 0,
    isLearned: Boolean = false,
    levelOfKnowledge: Int = 0,
    lastAnsweredAt: DateTime? = null
) : RegistrableFlowableState<Card>(), Copyable {
    var question: String by me(question)
    var answer: String by me(answer)
    var lap: Int by me(lap)
    var isLearned: Boolean by me(isLearned)
    var levelOfKnowledge: Int by me(levelOfKnowledge)
    var lastAnsweredAt: DateTime? by me(lastAnsweredAt)

    override fun copy() =
        Card(id, question, answer, lap, isLearned, levelOfKnowledge, lastAnsweredAt)
}