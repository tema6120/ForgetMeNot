import com.odnovolov.forgetmenot.persistence.usersessionterm.LastAnswerFilterDialogStateProvider
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.REPETITION_SETTINGS_SCOPE_ID
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.lastanswer.LastAnswerFilterController
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.lastanswer.LastAnswerFilterViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val lastAnswerFilterModule = module {
    scope<LastAnswerFilterViewModel> {
        scoped { LastAnswerFilterDialogStateProvider() }
        scoped { get<LastAnswerFilterDialogStateProvider>().load() }
        scoped {
            LastAnswerFilterController(
                repetitionSettings = getScope(REPETITION_SETTINGS_SCOPE_ID).get(),
                dialogState = get(),
                longTermStateSaver = get(),
                dialogStateProvider = get<LastAnswerFilterDialogStateProvider>()
            )
        }
        viewModel { LastAnswerFilterViewModel(dialogState = get()) }
    }
}

const val LAST_ANSWER_FILTER_SCOPE_ID = "LAST_ANSWER_FILTER_SCOPE_ID"