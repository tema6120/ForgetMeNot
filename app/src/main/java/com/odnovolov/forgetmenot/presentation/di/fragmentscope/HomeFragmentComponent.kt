package com.odnovolov.forgetmenot.presentation.di.fragmentscope

import com.odnovolov.forgetmenot.presentation.screen.HomeFragment
import dagger.Subcomponent

@FragmentScope
@Subcomponent(modules = [HomeFragmentModule::class])
interface HomeFragmentComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): HomeFragmentComponent
    }

    fun inject(homeFragment: HomeFragment)
}