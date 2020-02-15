package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import com.odnovolov.forgetmenot.domain.interactor.adddeck.AddDeckInteractor
import com.odnovolov.forgetmenot.presentation.common.Store
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.koin.dsl.onClose

val addDeckModule = module {
    scope<AddDeckViewModel> {
        scoped { get<Store>().loadAddDeckState() }
        scoped { get<Store>().loadAddDeckScreenState() }
        scoped { AddDeckInteractor(state = get(), globalState = get()) }
        scoped {
            AddDeckController(
                addDeckScreenState = get(),
                addDeckInteractor = get(),
                store = get()
            )
        } onClose { controller: AddDeckController? -> controller?.onCleared() }
        viewModel {
            AddDeckViewModel(
                globalState = get(),
                addDeckInteractorState = get(),
                addDeckScreenState = get()
            )
        }
    }
}

const val ADD_DECK_SCOPE_ID = "ADD_DECK_SCOPE_ID"