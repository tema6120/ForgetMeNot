package com.odnovolov.forgetmenot.presentation.screen.player.service

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player
import com.odnovolov.forgetmenot.domain.interactor.autoplay.PlayingCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

class PlayerServiceModel(
    private val playerState: Player.State
) {
    val question: Flow<String> = playerState.flowOf(Player.State::currentPosition)
        .flatMapLatest { position: Int ->
            val playingCard: PlayingCard = playerState.playingCards[position]
            with(playingCard) {
                if (isReverse)
                    card.flowOf(Card::answer) else
                    card.flowOf(Card::question)
            }
        }

    val isPlaying: Flow<Boolean> = playerState.flowOf(Player.State::isPlaying)

    val isCompleted: Flow<Boolean> = playerState.flowOf(Player.State::isCompleted)
}