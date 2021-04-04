package com.odnovolov.forgetmenot.presentation.screen.exampleexercise

import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.example.ExampleExercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.example.ExerciseExamplePurpose
import com.odnovolov.forgetmenot.persistence.shortterm.ExerciseExamplePurposeStateProvider
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
    exerciseExamplePurpose: ExerciseExamplePurpose? = null
) {
    private val exerciseStateProvider = ExerciseStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState,
        key = "ExampleExerciseState"
    )

    private val exerciseState: Exercise.State =
        initialExerciseState ?: exerciseStateProvider.load()

    private val exerciseExamplePurposeStateProvider = ExerciseExamplePurposeStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        key = "ExampleExerciseState purpose"
    )

    private val purpose: ExerciseExamplePurpose =
        exerciseExamplePurpose?.also(exerciseExamplePurposeStateProvider::save)
            ?: exerciseExamplePurposeStateProvider.load()

    val exercise = ExampleExercise(
        exerciseState,
        purpose,
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
        purpose,
        AppDiScope.get().speakerImpl,
        AppDiScope.get().walkingModePreference,
        AppDiScope.get().exerciseSettings,
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
            purpose: ExerciseExamplePurpose
        ) = ExampleExerciseDiScope(
            initialExerciseState,
            purpose
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