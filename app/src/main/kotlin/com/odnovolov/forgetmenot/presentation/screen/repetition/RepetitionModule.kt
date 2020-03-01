package com.odnovolov.forgetmenot.presentation.screen.repetition

import com.odnovolov.forgetmenot.domain.entity.Speaker
import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.common.Store
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
        scoped { get<Store>().loadRepetitionState(globalState = get()) }
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
                store = get()
            )
        } onClose { it?.onCleared() }
        scoped { RepetitionScopeCloser() }
        viewModel { RepetitionViewModel(repetitionScopeCloser = get(), repetitionState = get()) }
        scoped { RepetitionServiceController(repetition = get()) }
        scoped { RepetitionServiceModel(repetitionState = get()) }
    }
}

const val REPETITION_SCOPE_ID = "REPETITION_SCOPE_ID"