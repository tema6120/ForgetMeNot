package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import com.odnovolov.forgetmenot.domain.interactor.deckadder.DeckAdder
import com.odnovolov.forgetmenot.presentation.common.Store
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.koin.dsl.onClose

val addDeckModule = module {
    scope<AddDeckViewModel> {
        scoped { get<Store>().loadAddDeckState() }
        scoped { get<Store>().loadAddDeckScreenState() }
        scoped { DeckAdder(state = get(), globalState = get()) }
        scoped {
            AddDeckController(
                addDeckScreenState = get(),
                deckAdder = get(),
                navigator = get(),
                store = get()
            )
        } onClose { it?.onCleared() }
        viewModel {
            AddDeckViewModel(
                globalState = get(),
                deckAdderState = get(),
                addDeckScreenState = get()
            )
        }
    }
}

const val ADD_DECK_SCOPE_ID = "ADD_DECK_SCOPE_ID"