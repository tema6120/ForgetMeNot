package com.odnovolov.forgetmenot.presentation.screen.exercise

import com.odnovolov.forgetmenot.domain.entity.Speaker
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.persistence.shortterm.ExerciseStateProvider
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry.EntryTestExerciseCardController
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.manual.ManualTestExerciseCardController
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.off.OffTestExerciseCardController
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.quiz.QuizTestExerciseCardController
import kotlinx.coroutines.CoroutineScope
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.dsl.onClose

val exerciseModule = module {
    scope<ExerciseViewModel> {
        scoped { ExerciseStateProvider(globalState = get()) }
        scoped { get<ExerciseStateProvider>().load() }
        scoped { SpeakerImpl(applicationContext = get()) } bind Speaker::class onClose { it?.shutdown() }
        scoped { Exercise(state = get(), speaker = get()) }
        scoped {
            ExerciseController(
                exercise = get(),
                navigator = get(),
                longTermStateSaver = get(),
                walkingModePreference = get(),
                exerciseStateProvider = get<ExerciseStateProvider>()
            )
        }
        viewModel { ExerciseViewModel(exerciseState = get(), walkingModePreference = get()) }
        scoped { OffTestExerciseCardController(exercise = get(), longTermStateSaver = get()) }
        scoped { ManualTestExerciseCardController(exercise = get(), longTermStateSaver = get()) }
        scoped { QuizTestExerciseCardController(exercise = get(), longTermStateSaver = get()) }
        scoped { EntryTestExerciseCardController(exercise = get(), longTermStateSaver = get()) }
        factory { (coroutineScope: CoroutineScope) ->
            ExerciseCardAdapter(
                coroutineScope,
                offTestExerciseCardController = get(),
                manualTestExerciseCardController = get(),
                quizTestExerciseCardController = get(),
                entryTestExerciseCardController = get()
            )
        }
    }
}

const val EXERCISE_SCOPE_ID = "EXERCISE_SCOPE_ID"