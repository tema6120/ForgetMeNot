package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.interactor.prepareexercise.PrepareExerciseInteractor
import com.odnovolov.forgetmenot.domain.interactor.removedeck.RemoveDeckInteractor
import com.odnovolov.forgetmenot.presentation.common.Store
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    scope<HomeViewModel> {
        scoped { get<Store>().loadHomeScreenState() }
        factory { RemoveDeckInteractor(globalState = get()) }
        factory { PrepareExerciseInteractor(globalState = get()) }
        scoped {
            HomeController(
                homeScreenState = get(),
                deckReviewPreference = get(),
                removeDeckInteractor = get(),
                prepareExerciseInteractor = get(),
                navigator = get(),
                store = get()
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