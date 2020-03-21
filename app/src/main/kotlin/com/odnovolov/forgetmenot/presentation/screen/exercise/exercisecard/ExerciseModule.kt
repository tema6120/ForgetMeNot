package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard

import com.odnovolov.forgetmenot.presentation.screen.exercise.EXERCISE_SCOPE_ID
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val exerciseCardModule = module {
    scope<ExerciseCardViewModel> {
        scoped {
            ExerciseCardController(
                exercise = getScope(EXERCISE_SCOPE_ID).get(),
                longTermStateSaver = get()
            )
        }
        viewModel { (id: Long) ->
            ExerciseCardViewModel(
                exerciseState = getScope(EXERCISE_SCOPE_ID).get(),
                id = id
            )
        }
    }
}

const val EXERCISE_CARD_SCOPE_ID_PREFIX = "EXERCISE_CARD_SCOPE_#"