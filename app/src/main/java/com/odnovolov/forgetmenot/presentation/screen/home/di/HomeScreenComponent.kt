package com.odnovolov.forgetmenot.presentation.screen.home.di

import com.odnovolov.forgetmenot.presentation.di.fragmentscope.FragmentScope
import com.odnovolov.forgetmenot.presentation.screen.home.HomeFragment
import dagger.Subcomponent

@FragmentScope
@Subcomponent(modules = [HomeScreenModule::class])
interface HomeScreenComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): HomeScreenComponent
    }

    fun inject(fragment: HomeFragment)
}