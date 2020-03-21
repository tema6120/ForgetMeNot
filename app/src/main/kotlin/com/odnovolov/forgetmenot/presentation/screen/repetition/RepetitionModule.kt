package com.odnovolov.forgetmenot.presentation.screen.repetition

import com.odnovolov.forgetmenot.domain.entity.Speaker
import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.persistence.usersessionterm.RepetitionStateProvider
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.screen.repetition.service.RepetitionServiceController
import com.odnovolov.forgetmenot.presentation.screen.repetition.service.RepetitionServiceModel
import com.odnovolov.forgetmenot.presentation.screen.repetition.view.RepetitionViewController
import com.odnovolov.forgetmenot.presentation.screen.repetition.view.RepetitionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.dsl.onClose

val repetitionModule = module {
    scope<Repetition> {
        scoped { RepetitionStateProvider(globalState = get()) }
        scoped { get<RepetitionStateProvider>().load() }
        scoped {
            SpeakerImpl(applicationContext = get())
        } bind Speaker::class onClose { it?.shutdown() }
        scoped {
            Repetition(
                state = get(),
                speaker = get(),
                coroutineDispatcher = Dispatchers.Main
            )
        } onClose { it?.cancel() }
        scoped {
            RepetitionViewController(
                repetition = get(),
                longTermStateSaver = get(),
                repetitionStateProvider = get<RepetitionStateProvider>()
            )
        }
        scoped { RepetitionScopeCloser() }
        viewModel { RepetitionViewModel(repetitionScopeCloser = get(), repetitionState = get()) }
        scoped {
            RepetitionServiceController(
                repetition = get(),
                longTermStateSaver = get(),
                repetitionStateProvider = get<RepetitionStateProvider>()
            )
        }
        scoped { RepetitionServiceModel(repetitionState = get()) }
    }
}

const val REPETITION_SCOPE_ID = "REPETITION_SCOPE_ID"