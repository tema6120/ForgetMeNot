package com.odnovolov.forgetmenot.presentation.screen.exercise

import com.odnovolov.forgetmenot.domain.entity.Speaker
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.persistence.shortterm.ExerciseStateProvider
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
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
    }
}

const val EXERCISE_SCOPE_ID = "EXERCISE_SCOPE_ID"