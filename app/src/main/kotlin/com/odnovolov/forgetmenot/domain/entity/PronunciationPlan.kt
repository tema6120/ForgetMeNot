package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.architecturecomponents.RegistrableFlowableState
import com.odnovolov.forgetmenot.domain.entity.PronunciationEvent.*
import com.soywiz.klock.seconds

class PronunciationPlan(
    override val id: Long,
    name: String,
    pronunciationEvents: List<PronunciationEvent>
) : RegistrableFlowableState<PronunciationPlan>() {
    var name: String by me(name)
    var pronunciationEvents: List<PronunciationEvent> by me(
        pronunciationEvents,
        preferredChangeClass = PropertyValueChange::class
    )

    override fun copy() = PronunciationPlan(id, name, pronunciationEvents)

    companion object {
        val Default by lazy {
            PronunciationPlan(
                id = 0L,
                name = "",
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

fun PronunciationPlan.isIndividual(): Boolean = !isDefault() && name.isEmpty()

fun checkPronunciationPlanName(testedName: String, globalState: GlobalState): NameCheckResult {
    return when {
        testedName.isEmpty() -> NameCheckResult.Empty
        globalState.sharedPronunciationPlans.any { it.name == testedName } ->
            NameCheckResult.Occupied
        else -> NameCheckResult.Ok
    }
}