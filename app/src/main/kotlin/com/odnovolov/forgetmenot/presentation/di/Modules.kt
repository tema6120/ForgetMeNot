package com.odnovolov.forgetmenot.presentation.di

import com.odnovolov.forgetmenot.domain.interactor.adddeck.AddDeck
import com.odnovolov.forgetmenot.persistence.StoreImpl
import com.odnovolov.forgetmenot.presentation.common.Store
import com.odnovolov.forgetmenot.presentation.screen.home.HomeController
import com.odnovolov.forgetmenot.presentation.screen.home.HomeViewModel
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckController
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<Store> { StoreImpl() }
    single { get<Store>().loadGlobalState() }
    single { get<Store>().loadDeckReviewPreference() }
    viewModel { HomeViewModel(globalState = get(), deckReviewPreference = get()) }
    scope<HomeViewModel> {
        scoped { get<Store>().loadHomeScreenState() }
        factory {
            HomeController(
                homeScreenState = get(),
                deckReviewPreference = get(),
                store = get()
            )
        }
    }
    viewModel { AddDeckViewModel(globalState = get()) }
    scope<AddDeckViewModel> {
        scoped { get<Store>().loadAddDeckState() }
        scoped { get<Store>().loadAddDeckScreenState() }
        factory { AddDeck(state = get(), globalState = get()) }
        factory {
            AddDeckController(
                addDeckScreenState = get(),
                addDeck = get(),
                store = get()
            )
        }
    }
}