package com.odnovolov.forgetmenot.presentation.screen.exercise

import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.persistence.shortterm.ExerciseStateProvider
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry.EntryTestExerciseCardController
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.manual.ManualTestExerciseCardController
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.off.OffTestExerciseCardController
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.quiz.QuizTestExerciseCardController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

class ExerciseDiScope private constructor(
    initialExerciseState: Exercise.State? = null
) {
    private val exerciseStateProvider = ExerciseStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState
    )

    private val exerciseState: Exercise.State =
        initialExerciseState ?: exerciseStateProvider.load()

    private val speakerImpl = SpeakerImpl(
        AppDiScope.get().app,
        AppDiScope.get().activityLifecycleCallbacksInterceptor.activityLifecycleEventFlow
    )

    private val exercise = Exercise(
        exerciseState,
        AppDiScope.get().globalState,
        speakerImpl,
        coroutineContext = Job() + businessLogicThread
    )

    val controller = ExerciseController(
        exercise,
        AppDiScope.get().walkingModePreference,
        AppDiScope.get().globalState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        exerciseStateProvider
    )

    val viewModel = ExerciseViewModel(
        exerciseState,
        speakerImpl.state,
        AppDiScope.get().walkingModePreference,
        AppDiScope.get().globalState
    )

    private val offTestExerciseCardController = OffTestExerciseCardController(
        exercise,
        AppDiScope.get().longTermStateSaver,
        exerciseStateProvider
    )

    private val manualTestExerciseCardController = ManualTestExerciseCardController(
        exercise,
        AppDiScope.get().longTermStateSaver,
        exerciseStateProvider
    )

    private val quizTestExerciseCardController = QuizTestExerciseCardController(
        exercise,
        AppDiScope.get().longTermStateSaver,
        exerciseStateProvider
    )

    private val entryTestExerciseCardController = EntryTestExerciseCardController(
        exercise,
        AppDiScope.get().longTermStateSaver,
        exerciseStateProvider
    )

    fun getExerciseCardAdapter(coroutineScope: CoroutineScope) = ExerciseCardAdapter(
        coroutineScope,
        offTestExerciseCardController,
        manualTestExerciseCardController,
        quizTestExerciseCardController,
        entryTestExerciseCardController
    )

    companion object : DiScopeManager<ExerciseDiScope>() {
        fun create(initialExerciseState: Exercise.State) = ExerciseDiScope(initialExerciseState)

        fun shareExercise(): Exercise {
            if (diScope == null) {
                diScope = recreateDiScope()
            }
            return diScope!!.exercise
        }

        override fun recreateDiScope() = ExerciseDiScope()

        override fun onCloseDiScope(diScope: ExerciseDiScope) {
            with(diScope) {
                speakerImpl.shutdown()
                controller.dispose()
                offTestExerciseCardController.dispose()
                manualTestExerciseCardController.dispose()
                quizTestExerciseCardController.dispose()
                entryTestExerciseCardController.dispose()
            }
        }
    }
}