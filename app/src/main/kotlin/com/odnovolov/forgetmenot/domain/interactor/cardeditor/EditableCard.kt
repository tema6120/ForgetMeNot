package com.odnovolov.forgetmenot.domain.interactor.cardeditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck

class EditableCard(
    val card: Card,
    val deck: Deck?,
    question: String = card.question,
    answer: String = card.answer,
    isLearned: Boolean = card.isLearned,
    levelOfKnowledge: Int = card.grade
) : FlowMaker<EditableCard>() {
    var question: String by flowMaker(question)
    var answer: String by flowMaker(answer)
    var isLearned: Boolean by flowMaker(isLearned)
    var levelOfKnowledge: Int by flowMaker(levelOfKnowledge)
}

fun EditableCard.isBlank(): Boolean = question.isBlank() && answer.isBlank()
fun EditableCard.isUnderfilled(): Boolean = question.isBlank() xor answer.isBlank()