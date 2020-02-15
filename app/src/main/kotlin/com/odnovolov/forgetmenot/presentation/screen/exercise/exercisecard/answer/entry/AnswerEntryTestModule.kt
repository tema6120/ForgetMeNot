package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.entry

import com.odnovolov.forgetmenot.presentation.screen.exercise.EXERCISE_SCOPE_ID
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val answerEntryTestModule = module {
    scope<AnswerEntryTestViewModel> {
        scoped {
            AnswerEntryTestController(
                exercise = getScope(EXERCISE_SCOPE_ID).get(),
                store = get()
            )
        }
        viewModel { (id: Long) ->
            AnswerEntryTestViewModel(
                exerciseState = getScope(EXERCISE_SCOPE_ID).get(),
                id = id
            )
        }
    }
}

const val ANSWER_ENTRY_TEST_SCOPE_ID_PREFIX = "ANSWER_ENTRY_TEST_SCOPE_#"