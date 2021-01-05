package com.odnovolov.forgetmenot.presentation.screen.exampleexercise

import com.odnovolov.forgetmenot.domain.interactor.exercise.example.ExampleExercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.State
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.exampleexercise.ExampleExerciseEvent.*

class ExampleExerciseController(
    private val exercise: ExampleExercise,
    private val exerciseStateProvider: ShortTermStateProvider<State>
) : BaseController<ExampleExerciseEvent, Nothing>() {
    override fun handle(event: ExampleExerciseEvent) {
        when (event) {
            BottomSheetExpanded -> {
                exercise.begin()
            }

            BottomSheetCollapsed -> {
                exercise.end()
            }

            is PageSelected -> {
                exercise.setPosition(event.position)
            }

            SpeakButtonClicked -> {
                exercise.speak()
            }

            StopSpeakButtonClicked -> {
                exercise.stopSpeaking()
            }

            StopTimerButtonClicked -> {
                exercise.stopTimer()
            }

            FragmentResumed -> {
                exercise.startTimer()
            }

            FragmentPaused -> {
                exercise.resetTimer()
            }
        }
    }

    override fun saveState() {
        exerciseStateProvider.save(exercise.state)
    }
}