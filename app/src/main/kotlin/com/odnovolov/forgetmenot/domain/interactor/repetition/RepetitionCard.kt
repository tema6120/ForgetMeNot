package com.odnovolov.forgetmenot.domain.interactor.repetition

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck

class RepetitionCard(
    id: Long,
    card: Card,
    deck: Deck,
    isQuestionDisplayed: Boolean,
    isReverse: Boolean,
    isAnswered: Boolean = false
) : FlowableState<RepetitionCard>() {
    val id: Long by me(id)
    val card: Card by me(card)
    val deck: Deck by me(deck)
    var isQuestionDisplayed: Boolean by me(isQuestionDisplayed)
    val isReverse: Boolean by me(isReverse)
    var isAnswered: Boolean by me(isAnswered)
}