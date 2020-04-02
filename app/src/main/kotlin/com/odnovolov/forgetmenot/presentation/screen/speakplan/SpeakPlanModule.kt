import com.odnovolov.forgetmenot.domain.interactor.decksettings.SpeakPlanSettings
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DECK_SETTINGS_SCOPED_ID
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakEventDialogState
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakPlanController
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakPlanViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val speakPlanModule = module {
    scope<SpeakPlanViewModel> {
        scoped {
            SpeakPlanSettings(
                deckSettings = getScope(DECK_SETTINGS_SCOPED_ID).get(),
                globalState = get()
            )
        }
        scoped { SpeakEventDialogState() }
        scoped {
            SpeakPlanController(
                deckSettingsState = getScope(DECK_SETTINGS_SCOPED_ID).get(),
                speakPlanSettings = get(),
                dialogState = get(),
                navigator = get(),
                longTermStateSaver = get()
            )
        }
        viewModel {
            SpeakPlanViewModel(
                deckSettingsState = getScope(DECK_SETTINGS_SCOPED_ID).get(),
                dialogState = get()
            )
        }
    }
}

const val SPEAK_PLAN_SCOPE_ID = "SPEAK_PLAN_SCOPE_ID"