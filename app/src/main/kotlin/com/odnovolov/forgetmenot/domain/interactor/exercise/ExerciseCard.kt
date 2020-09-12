package com.odnovolov.forgetmenot.domain.interactor.exercise

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck

interface ExerciseCard {
    val base: Base

    class Base(
        id: Long,
        card: Card,
        deck: Deck,
        isReverse: Boolean,
        isQuestionDisplayed: Boolean,
        isAnswerCorrect: Boolean? = null,
        hint: String? = null,
        timeLeft: Int,
        isExpired: Boolean = false,
        initialLevelOfKnowledge: Int,
        isLevelOfKnowledgeEditedManually: Boolean
    ) : FlowMaker<Base>() {
        val id: Long by flowMaker(id)
        val card: Card by flowMaker(card)
        val deck: Deck by flowMaker(deck)
        val isReverse: Boolean by flowMaker(isReverse)
        var isQuestionDisplayed: Boolean by flowMaker(isQuestionDisplayed)
        var isAnswerCorrect: Boolean? by flowMaker(isAnswerCorrect)
        var hint: String? by flowMaker(hint)
        var timeLeft: Int by flowMaker(timeLeft)
        var isExpired: Boolean by flowMaker(isExpired)
        val initialLevelOfKnowledge: Int by flowMaker(initialLevelOfKnowledge)
        var isLevelOfKnowledgeEditedManually: Boolean by flowMaker(isLevelOfKnowledgeEditedManually)
    }
}

val ExerciseCard.isAnswered get() = base.isAnswerCorrect != null