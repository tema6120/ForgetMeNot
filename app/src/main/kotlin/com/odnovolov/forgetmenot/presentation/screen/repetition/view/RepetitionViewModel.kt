package com.odnovolov.forgetmenot.presentation.screen.repetition.view

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest

class RepetitionViewModel(
    private val repetitionState: Repetition.State
) {
    val repetitionCards: Flow<List<RepetitionCard>> =
        repetitionState.flowOf(Repetition.State::repetitionCards)

    private val currentRepetitionCard: Flow<RepetitionCard> = combine(
        repetitionCards,
        repetitionState.flowOf(Repetition.State::repetitionCardPosition)
    ) { repetitionCards: List<RepetitionCard>, position: Int ->
        repetitionCards[position]
    }
        .distinctUntilChanged()
        .share()

    val isCurrentRepetitionCardLearned: Flow<Boolean> =
        currentRepetitionCard.flatMapLatest { repetitionCard: RepetitionCard ->
            repetitionCard.card.flowOf(Card::isLearned)
        }

    val isPlaying: Flow<Boolean> = repetitionState.flowOf(Repetition.State::isPlaying)

    val repetitionCardPosition: Int
        get() = repetitionState.repetitionCardPosition
}