import com.odnovolov.forgetmenot.persistence.usersessionterm.RepetitionLapsDialogStateProvider
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.REPETITION_SETTINGS_SCOPE_ID
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.laps.RepetitionLapsController
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.laps.RepetitionLapsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val repetitionLapsModule = module {
    scope<RepetitionLapsViewModel> {
        scoped { RepetitionLapsDialogStateProvider() }
        scoped { get<RepetitionLapsDialogStateProvider>().load() }
        scoped {
            RepetitionLapsController(
                repetitionSettings = getScope(REPETITION_SETTINGS_SCOPE_ID).get(),
                dialogState = get(),
                dialogStateProvider = get<RepetitionLapsDialogStateProvider>(),
                longTermStateSaver = get()
            )
        }
        viewModel { RepetitionLapsViewModel(dialogState = get()) }
    }
}

const val REPETITION_LAPS_SCOPE_ID = "REPETITION_LAPS_SCOPE_ID"