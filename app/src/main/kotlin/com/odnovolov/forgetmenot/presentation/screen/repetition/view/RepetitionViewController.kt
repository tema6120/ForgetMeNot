package com.odnovolov.forgetmenot.presentation.screen.repetition.view

import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.screen.repetition.view.RepetitionViewController.Command.SetViewPagerPosition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combineTransform

class RepetitionViewController(
    private val repetition: Repetition,
    private val longTermStateSaver: LongTermStateSaver,
    private val repetitionStateProvider: ShortTermStateProvider<Repetition.State>
) {
    sealed class Command {
        class SetViewPagerPosition(val position: Int) : Command()
    }

    val commands: Flow<Command> = combineTransform(
        repetition.state.flowOf(Repetition.State::repetitionCardPosition),
        repetition.state.flowOf(Repetition.State::isPlaying)
    ) { position: Int, isPlaying: Boolean ->
        if (isPlaying) {
            emit(SetViewPagerPosition(position))
        }
    }

    fun onNewPageBecameSelected(position: Int) {
        repetition.setRepetitionCardPosition(position)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onShowAnswerButtonClicked() {
        repetition.showAnswer()
        longTermStateSaver.saveStateByRegistry()
    }

    fun onPauseButtonClicked() {
        repetition.pause()
        longTermStateSaver.saveStateByRegistry()
    }

    fun onResumeButtonClicked() {
        repetition.resume()
        longTermStateSaver.saveStateByRegistry()
    }

    fun onFragmentPause() {
        repetitionStateProvider.save(repetition.state)
    }
}