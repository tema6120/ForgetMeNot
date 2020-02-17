package com.odnovolov.forgetmenot.presentation.screen.exercise

import com.odnovolov.forgetmenot.domain.entity.Speaker
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.common.Store
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.dsl.onClose

val exerciseModule = module {
    scope<ExerciseViewModel> {
        scoped { get<Store>().loadExerciseState(globalState = get()) }
        scoped { SpeakerImpl(applicationContext = get()) } bind Speaker::class onClose { it?.shutdown() }
        scoped { Exercise(state = get(), speaker = get()) }
        scoped { ExerciseController(exercise = get(), navigator = get(), store = get(), walkingModePreference = get()) }
            .onClose { it?.onCleared() }
        viewModel { ExerciseViewModel(exerciseState = get(), walkingModePreference = get()) }
    }
}

const val EXERCISE_SCOPE_ID = "EXERCISE_SCOPE_ID"