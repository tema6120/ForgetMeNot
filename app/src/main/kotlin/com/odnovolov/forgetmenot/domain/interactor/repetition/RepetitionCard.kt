package com.odnovolov.forgetmenot.domain.interactor.repetition

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck

class RepetitionCard(
    id: Long,
    card: Card,
    deck: Deck,
    isQuestionDisplayed: Boolean,
    isReverse: Boolean,
    isAnswered: Boolean = false
) : FlowMaker<RepetitionCard>() {
    val id: Long by flowMaker(id)
    val card: Card by flowMaker(card)
    val deck: Deck by flowMaker(deck)
    var isQuestionDisplayed: Boolean by flowMaker(isQuestionDisplayed)
    val isReverse: Boolean by flowMaker(isReverse)
    var isAnswered: Boolean by flowMaker(isAnswered)
}