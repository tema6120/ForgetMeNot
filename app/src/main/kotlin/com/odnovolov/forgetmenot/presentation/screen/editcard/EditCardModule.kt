package com.odnovolov.forgetmenot.presentation.screen.editcard

import com.odnovolov.forgetmenot.presentation.screen.exercise.EXERCISE_SCOPE_ID
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val editCardModule = module {
    scope<EditCardViewModel> {
        scoped { EditCardScreenState() }
        scoped {
            EditCardController(
                editCardScreenState = get(),
                exercise = getScope(EXERCISE_SCOPE_ID).get(),
                navigator = get()
            )
        }
        viewModel { EditCardViewModel(editCardScreenState = get()) }
    }
}

const val EDIT_CARD_SCOPE_ID = "EDIT_CARD_SCOPE_ID"