package com.odnovolov.forgetmenot.presentation.screen.player.view.playingcard

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.autoplay.PlayingCard
import kotlinx.coroutines.flow.Flow

class PlayingCardViewModel(playingCard: PlayingCard) {
    val isQuestionDisplayed: Flow<Boolean> =
        playingCard.flowOf(PlayingCard::isQuestionDisplayed)

    val question: Flow<String> = with(playingCard) {
        if (isReverse)
            card.flowOf(Card::answer) else
            card.flowOf(Card::question)
    }

    val isAnswered: Flow<Boolean> = playingCard.flowOf(PlayingCard::isAnswered)

    val answer: Flow<String> = with(playingCard) {
        if (isReverse)
            card.flowOf(Card::question) else
            card.flowOf(Card::answer)
    }

    val isLearned: Flow<Boolean> = playingCard.card.flowOf(Card::isLearned)
}