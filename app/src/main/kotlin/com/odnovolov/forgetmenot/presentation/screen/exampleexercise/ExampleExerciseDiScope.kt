package com.odnovolov.forgetmenot.presentation.screen.exampleexercise

import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.example.ExampleExercise
import com.odnovolov.forgetmenot.persistence.shortterm.ExampleExerciseStateUseTimerProvider
import com.odnovolov.forgetmenot.persistence.shortterm.ExerciseStateProvider
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearance
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseCardAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

class ExampleExerciseDiScope private constructor(
    initialExerciseState: Exercise.State? = null,
    initialUseTimer: Boolean? = null
) {
    private val exerciseStateProvider = ExerciseStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState,
        key = "ExampleExerciseState"
    )

    private val exerciseState: Exercise.State =
        initialExerciseState ?: exerciseStateProvider.load()

    private val useTimerProvider = ExampleExerciseStateUseTimerProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        key = "ExampleExerciseState useTimer"
    )

    private val useTimer: Boolean =
        if (initialUseTimer != null) {
            useTimerProvider.save(initialUseTimer)
            initialUseTimer
        } else {
            useTimerProvider.load()
        }

    val exercise = ExampleExercise(
        exerciseState,
        useTimer,
        AppDiScope.get().speakerImpl,
        coroutineContext = Job() + businessLogicThread
    )

    val controller = ExampleExerciseController(
        exercise,
        exerciseStateProvider
    )

    private val cardAppearance: CardAppearance = AppDiScope.get().cardAppearance

    val viewModel = ExampleExerciseViewModel(
        exercise.state,
        useTimer,
        AppDiScope.get().speakerImpl,
        AppDiScope.get().walkingModePreference,
        AppDiScope.get().globalState
    )

    private val offTestCardController = OffTestExampleExerciseCardController(
        exercise,
        exerciseStateProvider
    )

    private val manualTestCardController = ManualTestExampleExerciseCardController(
        exercise,
        exerciseStateProvider
    )

    private val quizTestCardController = QuizTestExampleExerciseCardController(
        exercise,
        exerciseStateProvider
    )

    private val entryTestCardController = EntryTestExampleExerciseCardController(
        exercise,
        exerciseStateProvider
    )

    fun getExerciseCardAdapter(
        coroutineScope: CoroutineScope
    ) = ExerciseCardAdapter(
        coroutineScope,
        offTestCardController,
        manualTestCardController,
        quizTestCardController,
        entryTestCardController,
        cardAppearance
    )

    companion object : DiScopeManager<ExampleExerciseDiScope>() {
        fun create(
            initialExerciseState: Exercise.State,
            useTimer: Boolean
        ) = ExampleExerciseDiScope(
            initialExerciseState,
            useTimer
        )

        override fun recreateDiScope() = ExampleExerciseDiScope()

        override fun onCloseDiScope(diScope: ExampleExerciseDiScope) {
            with(diScope) {
                AppDiScope.get().speakerImpl.stop()
                exercise.cancel()
                controller.dispose()
                offTestCardController.dispose()
                manualTestCardController.dispose()
                quizTestCardController.dispose()
                entryTestCardController.dispose()
            }
        }
    }
}