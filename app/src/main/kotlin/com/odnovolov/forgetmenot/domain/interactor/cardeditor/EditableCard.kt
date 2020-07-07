package com.odnovolov.forgetmenot.domain.interactor.cardeditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.generateId

class EditableCard(
    val card: Card = Card(id = generateId(), question = "", answer = "")
) : FlowableState<EditableCard>() {
    var question: String by me(card.question)
    var answer: String by me(card.answer)
    var isLearned: Boolean by me(card.isLearned)
    var levelOfKnowledge: Int by me(card.levelOfKnowledge)
}