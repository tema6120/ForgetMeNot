package com.odnovolov.forgetmenot.presentation.screen.repetition.view

import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.presentation.common.StateProvider
import com.odnovolov.forgetmenot.presentation.screen.repetition.view.RepetitionViewController.Command.SetViewPagerPosition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combineTransform

class RepetitionViewController(
    private val repetition: Repetition,
    private val repetitionStateProvider: StateProvider<Repetition.State>
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

    private var isFragmentRemoving = false

    fun onNewPageBecameSelected(position: Int) {
        repetition.setRepetitionCardPosition(position)
    }

    fun onShowAnswerButtonClicked() {
        repetition.showAnswer()
    }

    fun onPauseButtonClicked() {
        repetition.pause()
    }

    fun onResumeButtonClicked() {
        repetition.resume()
    }

    fun onFragmentRemoving() {
        isFragmentRemoving = true
    }

    fun onCleared() {
        if (isFragmentRemoving) {
            repetitionStateProvider.delete()
        } else {
            repetitionStateProvider.save(repetition.state)
        }
    }
}