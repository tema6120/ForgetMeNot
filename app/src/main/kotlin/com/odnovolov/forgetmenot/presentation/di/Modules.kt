package com.odnovolov.forgetmenot.presentation.di

import com.odnovolov.forgetmenot.domain.interactor.adddeck.AddDeckInteractor
import com.odnovolov.forgetmenot.domain.interactor.removedeck.RemoveDeckInteractor
import com.odnovolov.forgetmenot.persistence.StoreImpl
import com.odnovolov.forgetmenot.presentation.common.Store
import com.odnovolov.forgetmenot.presentation.screen.home.HomeViewModel
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckController
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckViewModel
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSortingController
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSortingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<Store> { StoreImpl() }
    single { get<Store>().loadGlobalState() }
    single { get<Store>().loadDeckReviewPreference() }
    factory { RemoveDeckInteractor(globalState = get()) }
    viewModel {
        HomeViewModel(
            homeScreenState = get<Store>().loadHomeScreenState(),
            globalState = get(),
            deckReviewPreference = get(),
            removeDeckInteractor = get(),
            store = get()
        )
    }
    viewModel { AddDeckViewModel(globalState = get()) }
    scope<AddDeckViewModel> {
        scoped { get<Store>().loadAddDeckState() }
        scoped { get<Store>().loadAddDeckScreenState() }
        factory { AddDeckInteractor(state = get(), globalState = get()) }
        factory {
            AddDeckController(
                addDeckScreenState = get(),
                addDeckInteractor = get(),
                store = get()
            )
        }
    }
    factory { DeckSortingController(deckReviewPreference = get(), store = get()) }
    viewModel { DeckSortingViewModel(deckReviewPreference = get(), controller = get()) }
}