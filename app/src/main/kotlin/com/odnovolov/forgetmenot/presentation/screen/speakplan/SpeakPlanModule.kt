import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakPlanController
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakPlanViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val speakPlanModule = module {
    scope<SpeakPlanViewModel> {
        scoped { SpeakPlanController(longTermStateSaver = get()) }
        viewModel { SpeakPlanViewModel() }
    }
}

const val SPEAK_PLAN_SCOPE_ID = "SPEAK_PLAN_SCOPE_ID"