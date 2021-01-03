package com.odnovolov.forgetmenot.presentation.screen.example

import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NOT_TO_USE_TIMER
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseViewModel
import com.odnovolov.forgetmenot.presentation.screen.exercise.TimerStatus
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModePreference
import kotlinx.coroutines.flow.*

class ExampleExerciseViewModel(
    exerciseState: Exercise.State,
    useTimer: Boolean,
    speakerImpl: SpeakerImpl,
    walkingModePreference: WalkingModePreference,
    globalState: GlobalState
) : ExerciseViewModel(
    exerciseState,
    speakerImpl,
    walkingModePreference,
    globalState
) {
    override val timerStatus: Flow<TimerStatus> =
        if (!useTimer) {
            flowOf(TimerStatus.NotUsed)
        } else {
            currentExerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
                if (exerciseCard.base.deck.exercisePreference.timeForAnswer == NOT_TO_USE_TIMER) {
                    flowOf(TimerStatus.NotUsed)
                } else {
                    combine(
                        exerciseCard.base.flowOf(ExerciseCard.Base::timeLeft),
                        exerciseCard.base.flowOf(ExerciseCard.Base::isExpired)
                    ) { timeLeft: Int,
                        isExpired: Boolean
                        ->
                        when {
                            timeLeft > 0 -> TimerStatus.Ticking(timeLeft)
                            isExpired -> TimerStatus.TimeIsOver
                            else -> TimerStatus.Stopped
                        }
                    }
                }
            }
                .distinctUntilChanged()
                .flowOn(businessLogicThread)
        }
}