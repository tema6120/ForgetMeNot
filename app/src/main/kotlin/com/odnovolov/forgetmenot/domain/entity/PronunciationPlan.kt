package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import com.odnovolov.forgetmenot.domain.entity.PronunciationEvent.*
import com.soywiz.klock.seconds

class PronunciationPlan(
    override val id: Long,
    pronunciationEvents: List<PronunciationEvent>
) : FlowMakerWithRegistry<PronunciationPlan>() {
    var pronunciationEvents: List<PronunciationEvent> by flowMaker(
        pronunciationEvents,
        preferredChangeClass = PropertyValueChange::class
    )

    override fun copy() = PronunciationPlan(id, pronunciationEvents)

    companion object {
        val Default by lazy {
            PronunciationPlan(
                id = 0L,
                pronunciationEvents = listOf(
                    SpeakQuestion,
                    Delay(2.seconds),
                    SpeakAnswer,
                    Delay(1.seconds)
                )
            )
        }
    }
}

fun PronunciationPlan.isDefault(): Boolean = id == PronunciationPlan.Default.id