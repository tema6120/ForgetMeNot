package com.odnovolov.forgetmenot.presentation.common

import com.odnovolov.forgetmenot.persistence.StoreImpl
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single<Store> { StoreImpl() }
    single { get<Store>().loadGlobalState() }
    single { get<Store>().loadDeckReviewPreference() }
    single { NavigatorImpl() } bind Navigator::class
}