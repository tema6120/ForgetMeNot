package com.odnovolov.forgetmenot.domain.interactor.exercise

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import com.odnovolov.forgetmenot.domain.entity.*

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
        initialLevelOfKnowledge: Int,
        isLevelOfKnowledgeEditedManually: Boolean = false
    ) : FlowableState<Base>() {
        val id: Long by me(id)
        val card: Card by me(card)
        val deck: Deck by me(deck)
        val isReverse: Boolean by me(isReverse)
        var isQuestionDisplayed: Boolean by me (isQuestionDisplayed)
        var isAnswerCorrect: Boolean? by me(isAnswerCorrect)
        var hint: String? by me(hint)
        val initialLevelOfKnowledge: Int by me(initialLevelOfKnowledge)
        var isLevelOfKnowledgeEditedManually: Boolean by me(isLevelOfKnowledgeEditedManually)
    }
}