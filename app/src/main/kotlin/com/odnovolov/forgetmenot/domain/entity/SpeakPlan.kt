package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.RegistrableFlowableState
import com.odnovolov.forgetmenot.domain.entity.SpeakEvent.*

class SpeakPlan(
    override val id: Long,
    name: String,
    speakEvents: List<SpeakEvent>
) : RegistrableFlowableState<SpeakPlan>() {
    var name: String by me(name)
    var speakEvents: List<SpeakEvent> by me(speakEvents)

    override fun copy() = SpeakPlan(id, name, speakEvents)

    companion object {
        val Default by lazy {
            SpeakPlan(
                id = 0L,
                name = "",
                speakEvents = listOf(SpeakQuestion, Delay(2), SpeakAnswer, Delay(1))
            )
        }
    }
}