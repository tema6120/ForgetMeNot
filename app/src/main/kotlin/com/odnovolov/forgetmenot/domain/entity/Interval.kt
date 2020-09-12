package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import com.soywiz.klock.DateTimeSpan

class Interval(
    override val id: Long,
    levelOfKnowledge: Int,
    value: DateTimeSpan
) : FlowMakerWithRegistry<Interval>() {
    var levelOfKnowledge: Int by flowMaker(levelOfKnowledge)
    var value: DateTimeSpan by flowMaker(value)

    override fun copy() = Interval(id, levelOfKnowledge, value)
}