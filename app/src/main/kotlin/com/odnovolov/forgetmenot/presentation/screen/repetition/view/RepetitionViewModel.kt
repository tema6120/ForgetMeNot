package com.odnovolov.forgetmenot.presentation.screen.repetition.view

import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionCard
import com.odnovolov.forgetmenot.presentation.screen.repetition.RepetitionScopeCloser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class RepetitionViewModel(
    private val repetitionScopeCloser: RepetitionScopeCloser,
    private val repetitionState: Repetition.State
) : ViewModel() {
    val isPlaying: Flow<Boolean> = repetitionState.flowOf(Repetition.State::isPlaying)

    val repetitionCards: Flow<List<RepetitionCard>> = combine(
        repetitionState.repetitionCards.map { it.asFlow() } // todo
    ) {
        repetitionState.repetitionCards
    }

    val repetitionCardPosition: Int
        get() = repetitionState.repetitionCardPosition

    override fun onCleared() {
        repetitionScopeCloser.isFragmentAlive = false
    }
}