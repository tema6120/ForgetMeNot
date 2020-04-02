package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import com.odnovolov.forgetmenot.domain.interactor.deckadder.DeckAdder
import com.odnovolov.forgetmenot.persistence.shortterm.AddDeckScreenStateProvider
import com.odnovolov.forgetmenot.persistence.shortterm.AddDeckStateProvider
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.koin.dsl.onClose

val addDeckModule = module {
    scope<AddDeckViewModel> {
        scoped { AddDeckStateProvider() }
        scoped { get<AddDeckStateProvider>().load() }
        scoped { AddDeckScreenStateProvider() }
        scoped { get<AddDeckScreenStateProvider>().load() }
        scoped { DeckAdder(state = get(), globalState = get()) }
        scoped {
            AddDeckController(
                addDeckScreenState = get(),
                deckAdder = get(),
                navigator = get(),
                longTermStateSaver = get(),
                addDeckStateProvider = get<AddDeckStateProvider>(),
                addDeckScreenStateProvider = get<AddDeckScreenStateProvider>()
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