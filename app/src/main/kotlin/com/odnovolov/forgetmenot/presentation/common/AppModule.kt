package com.odnovolov.forgetmenot.presentation.common

import com.odnovolov.forgetmenot.persistence.longterm.LongTermStateSaverImpl
import com.odnovolov.forgetmenot.persistence.longterm.deckreviewpreference.DeckReviewPreferenceProvider
import com.odnovolov.forgetmenot.persistence.longterm.globalstate.provision.GlobalStateProvider
import com.odnovolov.forgetmenot.persistence.longterm.walkingmodepreference.WalkingModePreferenceProvider
import org.koin.dsl.module

val appModule = module {
    single { GlobalStateProvider.load() }
    single { DeckReviewPreferenceProvider.load() }
    single { WalkingModePreferenceProvider.load() }
    single<LongTermStateSaver> { LongTermStateSaverImpl }
    single { Navigator() }
}