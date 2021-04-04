package com.odnovolov.forgetmenot.presentation.screen.exampleexercise

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.ExercisePreference
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.DO_NOT_USE_TIMER
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.domain.interactor.exercise.example.ExerciseExamplePurpose
import com.odnovolov.forgetmenot.domain.interactor.exercise.example.ExerciseExamplePurpose.ToDemonstrateGradingSettings
import com.odnovolov.forgetmenot.domain.interactor.exercise.example.ExerciseExamplePurpose.ToDemonstrateTimerSettings
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseViewModel
import com.odnovolov.forgetmenot.presentation.screen.exercise.TimerStatus
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.ExerciseSettings
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModePreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class ExampleExerciseViewModel(
    exerciseState: Exercise.State,
    private val purpose: ExerciseExamplePurpose,
    speakerImpl: SpeakerImpl,
    walkingModePreference: WalkingModePreference,
    exerciseSettings: ExerciseSettings,
    globalState: GlobalState
) : ExerciseViewModel(
    exerciseState,
    speakerImpl,
    walkingModePreference,
    exerciseSettings,
    globalState
) {
    val isGradeButtonVisible: Boolean
        get() = purpose == ToDemonstrateGradingSettings

    override val timerStatus: Flow<TimerStatus> =
        if (purpose != ToDemonstrateTimerSettings) {
            flowOf(TimerStatus.NotUsed)
        } else {
            currentExerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
                exerciseCard.base.deck.flowOf(Deck::exercisePreference)
                    .flatMapLatest { exercisePreference: ExercisePreference ->
                        exercisePreference.flowOf(ExercisePreference::timeForAnswer)
                    }
                    .flatMapLatest { timeForAnswer: Int ->
                        if (timeForAnswer == DO_NOT_USE_TIMER) {
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
            }
                .distinctUntilChanged()
                .flowOn(Dispatchers.Default)
        }
}