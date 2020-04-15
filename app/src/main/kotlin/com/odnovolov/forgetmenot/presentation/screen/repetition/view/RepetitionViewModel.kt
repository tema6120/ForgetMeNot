package com.odnovolov.forgetmenot.presentation.screen.repetition.view

import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class RepetitionViewModel(
    private val repetitionState: Repetition.State
) {
    val isPlaying: Flow<Boolean> = repetitionState.flowOf(Repetition.State::isPlaying)

    val repetitionCards: Flow<List<RepetitionCard>> = combine(
        repetitionState.repetitionCards.map { it.asFlow() } // todo
    ) {
        repetitionState.repetitionCards
    }

    val repetitionCardPosition: Int
        get() = repetitionState.repetitionCardPosition
}