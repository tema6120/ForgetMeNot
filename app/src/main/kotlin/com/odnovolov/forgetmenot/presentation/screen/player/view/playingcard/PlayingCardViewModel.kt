package com.odnovolov.forgetmenot.presentation.screen.player.view.playingcard

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.autoplay.PlayingCard
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import com.odnovolov.forgetmenot.presentation.screen.player.view.playingcard.CardContent.AnsweredCard
import com.odnovolov.forgetmenot.presentation.screen.player.view.playingcard.CardContent.UnansweredCard
import kotlinx.coroutines.flow.*

class PlayingCardViewModel(
    initialPlayingCard: PlayingCard
) {
    private val playingCardFlow = MutableStateFlow(initialPlayingCard)

    fun setPlayingCard(playingCard: PlayingCard) {
        playingCardFlow.value = playingCard
    }

    val cardContent: Flow<CardContent> = playingCardFlow.flatMapLatest { playingCard ->
        val isReverse: Boolean = playingCard.isReverse
        combine(
            playingCard.card.flowOf(Card::question),
            playingCard.card.flowOf(Card::answer),
            playingCard.flowOf(PlayingCard::isAnswerDisplayed)
        ) { question: String,
            answer: String,
            isAnswerDisplayed: Boolean
            ->
            val realQuestion = if (isReverse) answer else question
            val realAnswer = if (isReverse) question else answer
            when {
                isAnswerDisplayed -> AnsweredCard(realQuestion, realAnswer)
                else -> UnansweredCard(realQuestion)
            }
        }
    }
        .distinctUntilChanged()
        .flowOn(businessLogicThread)

    val isQuestionDisplayed: Flow<Boolean> = playingCardFlow.flatMapLatest { playingCard ->
        playingCard.flowOf(PlayingCard::isQuestionDisplayed)
    }
        .distinctUntilChanged()
        .flowOn(businessLogicThread)

    val isLearned: Flow<Boolean> = playingCardFlow.flatMapLatest { playingCard ->
        playingCard.card.flowOf(Card::isLearned)
    }
        .distinctUntilChanged()
        .flowOn(businessLogicThread)
}