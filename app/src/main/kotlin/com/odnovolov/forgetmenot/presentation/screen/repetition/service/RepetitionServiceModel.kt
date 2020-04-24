package com.odnovolov.forgetmenot.presentation.screen.repetition.service

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

class RepetitionServiceModel(
    private val repetitionState: Repetition.State
) {
    val question: Flow<String> = repetitionState.flowOf(Repetition.State::repetitionCardPosition)
        .flatMapLatest { position: Int ->
            val repetitionCard: RepetitionCard = repetitionState.repetitionCards[position]
            with(repetitionCard) {
                if (isReverse)
                    card.flowOf(Card::question) else
                    card.flowOf(Card::answer)
            }
        }

    val isPlaying: Flow<Boolean> = repetitionState.flowOf(Repetition.State::isPlaying)
}