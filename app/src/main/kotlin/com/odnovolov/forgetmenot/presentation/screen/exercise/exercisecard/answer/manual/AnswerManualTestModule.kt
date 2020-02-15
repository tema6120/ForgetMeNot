package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.manual

import com.odnovolov.forgetmenot.presentation.screen.exercise.EXERCISE_SCOPE_ID
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val answerManualTestModule = module {
    scope<AnswerManualTestViewModel> {
        scoped {
            AnswerManualTestController(
                exercise = getScope(EXERCISE_SCOPE_ID).get(),
                store = get()
            )
        }
        viewModel { (id: Long) ->
            AnswerManualTestViewModel(
                exerciseState = getScope(EXERCISE_SCOPE_ID).get(),
                id = id
            )
        }
    }
}

const val ANSWER_MANUAL_TEST_SCOPE_ID_PREFIX = "ANSWER_MANUAL_TEST_SCOPE_#"