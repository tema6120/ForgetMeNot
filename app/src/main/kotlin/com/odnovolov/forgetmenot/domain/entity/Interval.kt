package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.Copyable
import com.odnovolov.forgetmenot.domain.architecturecomponents.RegistrableFlowableState
import com.soywiz.klock.DateTimeSpan

class Interval(
    override val id: Long,
    targetLevelOfKnowledge: Int,
    value: DateTimeSpan
) : RegistrableFlowableState<Interval>(), Copyable {
    var targetLevelOfKnowledge: Int by me(targetLevelOfKnowledge)
    var value: DateTimeSpan by me(value)

    override fun copy() = Interval(id, targetLevelOfKnowledge, value)
}