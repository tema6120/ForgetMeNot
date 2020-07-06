package com.odnovolov.forgetmenot.domain.interactor.cardeditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import com.odnovolov.forgetmenot.domain.entity.Card
import com.soywiz.klock.DateTime

class EditableCard(
    val card: Card? = null
) : FlowableState<EditableCard>() {
    var question: String by me(card?.question ?: "")
    var answer: String by me(card?.answer ?: "")
    var lap: Int by me(card?.lap ?: 0)
    var isLearned: Boolean by me(card?.isLearned ?: false)
    var levelOfKnowledge: Int by me(card?.levelOfKnowledge ?: 0)
    var lastAnsweredAt: DateTime? by me(card?.lastAnsweredAt)
}