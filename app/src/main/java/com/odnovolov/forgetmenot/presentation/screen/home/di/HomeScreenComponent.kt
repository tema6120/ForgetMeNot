package com.odnovolov.forgetmenot.presentation.screen.home.di

import com.badoo.mvicore.android.AndroidTimeCapsule
import com.odnovolov.forgetmenot.presentation.di.appscope.AppComponent
import com.odnovolov.forgetmenot.presentation.di.fragmentscope.FragmentScope
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSortingBottomSheet
import com.odnovolov.forgetmenot.presentation.screen.home.HomeFragment
import dagger.BindsInstance
import dagger.Subcomponent

@FragmentScope
@Subcomponent(modules = [HomeScreenModule::class])
interface HomeScreenComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun with(timeCapsule: AndroidTimeCapsule): Builder

        fun build(): HomeScreenComponent
    }

    fun inject(fragment: HomeFragment)
    fun inject(bottomSheet: DeckSortingBottomSheet)

    companion object {
        private var INSTANCE: HomeScreenComponent? = null

        fun createWith(timeCapsule: AndroidTimeCapsule): HomeScreenComponent {
            INSTANCE = AppComponent.get()
                .homeScreenComponentBuilder()
                .with(timeCapsule)
                .build()
            return INSTANCE!!
        }

        fun get() = INSTANCE

        fun destroy() {
            INSTANCE = null
        }
    }
}