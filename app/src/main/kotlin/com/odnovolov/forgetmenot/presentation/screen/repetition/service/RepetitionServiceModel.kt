package com.odnovolov.forgetmenot.presentation.screen.repetition.service

import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RepetitionServiceModel(
    private val repetitionState: Repetition.State
) {
    val question: Flow<String> = repetitionState.flowOf(Repetition.State::repetitionCardPosition)
        .map { position: Int ->
            val repetitionCard: RepetitionCard = repetitionState.repetitionCards[position]
            with(repetitionCard) { if (isReverse) card.answer else card.question }
        }

    val isPlaying: Flow<Boolean> = repetitionState.flowOf(Repetition.State::isPlaying)
}