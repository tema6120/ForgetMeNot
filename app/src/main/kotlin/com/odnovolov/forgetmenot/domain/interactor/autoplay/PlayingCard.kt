package com.odnovolov.forgetmenot.domain.interactor.autoplay

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck

class PlayingCard(
    id: Long,
    card: Card,
    deck: Deck,
    isQuestionDisplayed: Boolean,
    isInverted: Boolean,
    isAnswerDisplayed: Boolean = false
) : FlowMaker<PlayingCard>() {
    val id: Long by flowMaker(id)
    val card: Card by flowMaker(card)
    val deck: Deck by flowMaker(deck)
    var isQuestionDisplayed: Boolean by flowMaker(isQuestionDisplayed)
    var isInverted: Boolean by flowMaker(isInverted)
    var isAnswerDisplayed: Boolean by flowMaker(isAnswerDisplayed)
}