package com.odnovolov.forgetmenot.presentation.screen.player.service

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player
import com.odnovolov.forgetmenot.domain.interactor.autoplay.PlayingCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class PlayerServiceModel(
    private val playerState: Player.State
) {
    val cardPosition: Flow<String?> = combine(
        playerState.flowOf(Player.State::currentPosition),
        playerState.flowOf(Player.State::playingCards)
    ) { currentPosition: Int, playingCards: List<PlayingCard> ->
        if (playingCards.isNotEmpty()) {
            "${currentPosition + 1}/${playingCards.size}"
        } else {
            null
        }
    }

    val question: Flow<String> = playerState.flowOf(Player.State::currentPosition)
        .flatMapLatest { position: Int ->
            val playingCard: PlayingCard? = playerState.playingCards.getOrNull(position)
            if (playingCard != null) {
                playingCard.flowOf(PlayingCard::isInverted)
                    .flatMapLatest { isInverted: Boolean ->
                        with(playingCard) {
                            if (isInverted)
                                card.flowOf(Card::answer) else
                                card.flowOf(Card::question)
                        }
                    }
            } else {
                flowOf("")
            }
        }

    val isPlaying: Flow<Boolean> = playerState.flowOf(Player.State::isPlaying)

    val isCompleted: Flow<Boolean> = playerState.flowOf(Player.State::isCompleted)
}