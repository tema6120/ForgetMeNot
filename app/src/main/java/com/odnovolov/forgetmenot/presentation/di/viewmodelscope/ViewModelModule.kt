package com.odnovolov.forgetmenot.presentation.di.viewmodelscope

import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature
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
    fun provideBinding(feature1: AddNewDeckFeature, feature2: DecksPreviewFeature,
                       liveDataProvider: LiveDataProvider): HomeViewModelBinding {
        return HomeViewModelBinding(feature1, feature2, liveDataProvider)
    }
}