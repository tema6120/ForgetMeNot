package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.RegistrableFlowableState
import com.soywiz.klock.DateTimeSpan

class Interval(
    override val id: Long,
    levelOfKnowledge: Int,
    value: DateTimeSpan
) : RegistrableFlowableState<Interval>() {
    var levelOfKnowledge: Int by me(levelOfKnowledge)
    var value: DateTimeSpan by me(value)

    override fun copy() = Interval(id, levelOfKnowledge, value)
}