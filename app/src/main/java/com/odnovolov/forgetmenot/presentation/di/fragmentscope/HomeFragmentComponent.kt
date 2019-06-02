package com.odnovolov.forgetmenot.presentation.di.fragmentscope

import com.odnovolov.forgetmenot.presentation.navigation.Navigator
import com.odnovolov.forgetmenot.presentation.screen.home.HomeFragment
import dagger.BindsInstance
import dagger.Subcomponent

@FragmentScope
@Subcomponent(modules = [HomeFragmentModule::class])
interface HomeFragmentComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun navigator(navigator: Navigator): Builder

        fun build(): HomeFragmentComponent
    }

    fun inject(homeFragment: HomeFragment)
}