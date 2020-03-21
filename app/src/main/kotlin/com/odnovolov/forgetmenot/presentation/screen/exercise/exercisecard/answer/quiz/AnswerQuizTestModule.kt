package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.quiz

import com.odnovolov.forgetmenot.presentation.screen.exercise.EXERCISE_SCOPE_ID
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val answerQuizTestModule = module {
    scope<AnswerQuizTestViewModel> {
        scoped { (id: Long) ->
            AnswerQuizTestController(
                id = id,
                exercise = getScope(EXERCISE_SCOPE_ID).get(),
                longTermStateSaver = get()
            )
        }
        viewModel { (id: Long) ->
            AnswerQuizTestViewModel(
                exerciseState = getScope(EXERCISE_SCOPE_ID).get(),
                id = id
            )
        }
    }
}

const val ANSWER_QUIZ_TEST_SCOPE_ID_PREFIX = "ANSWER_QUIZ_TEST_SCOPE_#"