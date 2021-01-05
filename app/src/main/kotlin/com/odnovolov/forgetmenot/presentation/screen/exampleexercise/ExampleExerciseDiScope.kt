package com.odnovolov.forgetmenot.presentation.screen.exampleexercise

import com.odnovolov.forgetmenot.domain.interactor.exercise.example.ExampleExercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.example.ExampleExerciseStateCreator
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.persistence.shortterm.ExampleExerciseStateProvider
import com.odnovolov.forgetmenot.persistence.shortterm.ExerciseStateProvider
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseCardAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

class ExampleExerciseDiScope private constructor(
    isRecreated: Boolean,
    initialUseTimer: Boolean? = null
) {
    private val exerciseStateProvider = ExerciseStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState
    )

    private val exerciseState: Exercise.State? =
        if (isRecreated) exerciseStateProvider.load() else null

    private val exampleExerciseStateProvider = ExampleExerciseStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val useTimer: Boolean =
        if (initialUseTimer != null) {
            exampleExerciseStateProvider.save(initialUseTimer)
            initialUseTimer
        } else {
            exampleExerciseStateProvider.load()
        }

    private val speakerImpl = SpeakerImpl(
        AppDiScope.get().app,
        AppDiScope.get().activityLifecycleCallbacksInterceptor.activityLifecycleEventFlow
    )

    private val exerciseStateCreator = ExampleExerciseStateCreator(
        DeckSettingsDiScope.get()!!.deckSettings.state.deck
    )

    val exercise = ExampleExercise(
        exerciseStateCreator,
        exerciseState,
        useTimer,
        speakerImpl,
        coroutineContext = Job() + businessLogicThread
    )

    val controller = ExampleExerciseController(
        exercise,
        exerciseStateProvider
    )

    val viewModel = ExampleExerciseViewModel(
        exercise.state,
        useTimer,
        speakerImpl,
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
        entryTestCardController
    )

    companion object : DiScopeManager<ExampleExerciseDiScope>() {
        fun create(useTimer: Boolean) = ExampleExerciseDiScope(isRecreated = false, useTimer)

        override fun recreateDiScope() = ExampleExerciseDiScope(isRecreated = true)

        override fun onCloseDiScope(diScope: ExampleExerciseDiScope) {
            with(diScope) {
                exercise.cancel()
                speakerImpl.shutdown()
                controller.dispose()
                offTestCardController.dispose()
                manualTestCardController.dispose()
                quizTestCardController.dispose()
                entryTestCardController.dispose()
            }
        }
    }
}