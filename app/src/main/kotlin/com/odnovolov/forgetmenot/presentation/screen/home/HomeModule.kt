package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.interactor.deckremover.DeckRemover
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseStateCreator
import com.odnovolov.forgetmenot.persistence.serializablestate.HomeScreenStateProvider
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    scope<HomeViewModel> {
        scoped { HomeScreenStateProvider() }
        scoped { get<HomeScreenStateProvider>().load() }
        scoped { DeckRemover(globalState = get()) }
        scoped { ExerciseStateCreator(globalState = get()) }
        scoped {
            HomeController(
                homeScreenState = get(),
                deckReviewPreference = get(),
                deckRemover = get(),
                exerciseStateCreator = get(),
                globalState = get(),
                navigator = get(),
                store = get(),
                homeScreenStateProvider = get<HomeScreenStateProvider>()
            )
        }
        viewModel {
            HomeViewModel(
                homeScreenState = get(),
                globalState = get(),
                deckReviewPreference = get(),
                controller = get()
            )
        }
        factory { DeckPreviewAdapter(controller = get()) }
    }
}

const val HOME_SCREEN_SCOPE_ID = "HOME_SCREEN_SCOPE_ID"