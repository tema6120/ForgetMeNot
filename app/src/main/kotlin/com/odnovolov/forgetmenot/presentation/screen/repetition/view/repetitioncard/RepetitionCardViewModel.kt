package com.odnovolov.forgetmenot.presentation.screen.repetition.view.repetitioncard

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionCard
import kotlinx.coroutines.flow.Flow

class RepetitionCardViewModel(repetitionCard: RepetitionCard) {
    val isQuestionDisplayed: Flow<Boolean> =
        repetitionCard.flowOf(RepetitionCard::isQuestionDisplayed)

    val question: Flow<String> = with(repetitionCard) {
        if (isReverse)
            card.flowOf(Card::answer) else
            card.flowOf(Card::question)
    }

    val isAnswered: Flow<Boolean> = repetitionCard.flowOf(RepetitionCard::isAnswered)

    val answer: Flow<String> = with(repetitionCard) {
        if (isReverse)
            card.flowOf(Card::question) else
            card.flowOf(Card::answer)
    }

    val isLearned: Flow<Boolean> = repetitionCard.card.flowOf(Card::isLearned)
}