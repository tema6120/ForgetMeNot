package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.architecturecomponents.RegistrableFlowableState
import com.odnovolov.forgetmenot.domain.entity.SpeakEvent.*
import com.soywiz.klock.seconds

class SpeakPlan(
    override val id: Long,
    name: String,
    speakEvents: List<SpeakEvent>
) : RegistrableFlowableState<SpeakPlan>() {
    var name: String by me(name)
    var speakEvents: List<SpeakEvent> by me(
        speakEvents,
        preferredChangeClass = PropertyValueChange::class
    )

    override fun copy() = SpeakPlan(id, name, speakEvents)

    companion object {
        val Default by lazy {
            SpeakPlan(
                id = 0L,
                name = "",
                speakEvents = listOf(SpeakQuestion, Delay(2.seconds), SpeakAnswer, Delay(1.seconds))
            )
        }
    }
}

fun SpeakPlan.isDefault(): Boolean = id == SpeakPlan.Default.id

fun SpeakPlan.isIndividual(): Boolean = !isDefault() && name.isEmpty()

fun checkSpeakPlanName(testedName: String, globalState: GlobalState): NameCheckResult {
    return when {
        testedName.isEmpty() -> NameCheckResult.Empty
        globalState.sharedSpeakPlans.any { it.name == testedName } -> NameCheckResult.Occupied
        else -> NameCheckResult.Ok
    }
}