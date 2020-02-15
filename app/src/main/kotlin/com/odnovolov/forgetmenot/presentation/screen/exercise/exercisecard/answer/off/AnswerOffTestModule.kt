package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.off

import com.odnovolov.forgetmenot.presentation.screen.exercise.EXERCISE_SCOPE_ID
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val answerOffTestModule = module {
    scope<AnswerOffTestViewModel> {
        scoped {
            AnswerOffTestController(
                exercise = getScope(EXERCISE_SCOPE_ID).get(),
                store = get()
            )
        }
        viewModel { (id: Long) ->
            AnswerOffTestViewModel(
                exerciseState = getScope(EXERCISE_SCOPE_ID).get(),
                id = id
            )
        }
    }
}

const val ANSWER_OFF_TEST_SCOPE_ID_PREFIX = "ANSWER_OFF_TEST_SCOPE_#"