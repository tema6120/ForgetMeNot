package com.odnovolov.forgetmenot.domain.interactor.cardeditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck

class EditableCard(
    val card: Card,
    val deck: Deck?,
    question: String = card.question,
    answer: String = card.answer,
    isLearned: Boolean = card.isLearned,
    levelOfKnowledge: Int = card.levelOfKnowledge
) : FlowableState<EditableCard>() {
    var question: String by me(question)
    var answer: String by me(answer)
    var isLearned: Boolean by me(isLearned)
    var levelOfKnowledge: Int by me(levelOfKnowledge)
}

fun EditableCard.isBlank(): Boolean = question.isBlank() && answer.isBlank()
fun EditableCard.isUnderfilled(): Boolean = question.isBlank() xor answer.isBlank()