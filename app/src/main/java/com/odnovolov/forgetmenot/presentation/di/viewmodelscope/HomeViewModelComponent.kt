package com.odnovolov.forgetmenot.presentation.di.viewmodelscope

import com.odnovolov.forgetmenot.presentation.di.fragmentscope.HomeFragmentComponent
import com.odnovolov.forgetmenot.presentation.screen.binding.HomeViewModelBinding
import com.odnovolov.forgetmenot.presentation.screen.binding.LiveDataProvider
import dagger.Subcomponent

@ViewModelScope
@Subcomponent(modules = [ViewModelModule::class, FeatureModule::class])
interface HomeViewModelComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): HomeViewModelComponent
    }

    fun homeFragmentComponentBuilder(): HomeFragmentComponent.Builder
    fun provideLiveDataProvider(): LiveDataProvider
    fun provideBinding(): HomeViewModelBinding
}