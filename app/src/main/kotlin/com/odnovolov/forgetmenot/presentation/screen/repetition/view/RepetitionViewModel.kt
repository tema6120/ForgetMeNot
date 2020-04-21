package com.odnovolov.forgetmenot.presentation.screen.repetition.view

import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionCard
import kotlinx.coroutines.flow.Flow

class RepetitionViewModel(
    private val repetitionState: Repetition.State
) {
    val isPlaying: Flow<Boolean> = repetitionState.flowOf(Repetition.State::isPlaying)

    val repetitionCards: Flow<List<RepetitionCard>> =
        repetitionState.flowOf(Repetition.State::repetitionCards)

    val repetitionCardPosition: Int
        get() = repetitionState.repetitionCardPosition
}