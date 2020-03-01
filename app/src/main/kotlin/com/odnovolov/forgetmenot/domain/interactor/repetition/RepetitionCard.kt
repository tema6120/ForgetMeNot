package com.odnovolov.forgetmenot.domain.interactor.repetition

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Pronunciation
import com.odnovolov.forgetmenot.domain.entity.SpeakPlan

class RepetitionCard(
    id: Long,
    card: Card,
    isAnswered: Boolean = false,
    isReverse: Boolean,
    pronunciation: Pronunciation,
    speakPlan: SpeakPlan
) : FlowableState<RepetitionCard>() {
    val id: Long by me(id)
    val card: Card by me(card)
    var isAnswered: Boolean by me(isAnswered)
    val isReverse: Boolean by me(isReverse)
    val pronunciation: Pronunciation by me(pronunciation)
    val speakPlan: SpeakPlan by me(speakPlan)
}