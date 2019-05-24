package com.odnovolov.forgetmenot.presentation.di.viewmodelscope

import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature
import com.odnovolov.forgetmenot.presentation.screen.binding.HomeViewModelBinding
import com.odnovolov.forgetmenot.presentation.screen.binding.LiveDataProvider
import dagger.Module
import dagger.Provides

@Module
class ViewModelModule {

    @ViewModelScope
    @Provides
    fun provideLiveDataProvider(): LiveDataProvider {
        return LiveDataProvider()
    }

    @ViewModelScope
    @Provides
    fun provideBinding(feature: AddNewDeckFeature,
                       liveDataProvider: LiveDataProvider): HomeViewModelBinding {
        return HomeViewModelBinding(feature, liveDataProvider)
    }
}