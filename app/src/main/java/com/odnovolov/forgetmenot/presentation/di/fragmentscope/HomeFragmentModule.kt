package com.odnovolov.forgetmenot.presentation.di.fragmentscope

import com.odnovolov.forgetmenot.data.repository.DeckRepositoryImpl
import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature
import com.odnovolov.forgetmenot.presentation.screen.DecksPreviewAdapter
import com.odnovolov.forgetmenot.presentation.screen.binding.HomeFragmentBinding
import com.odnovolov.forgetmenot.presentation.screen.binding.LiveDataProvider
import dagger.Module
import dagger.Provides

@Module
class HomeFragmentModule {

    @FragmentScope
    @Provides
    fun provideFeature(repository: DeckRepositoryImpl): AddNewDeckFeature {
        return AddNewDeckFeature(repository)
    }

    @FragmentScope
    @Provides
    fun provideBinding(feature: AddNewDeckFeature,
                       liveDataProvider: LiveDataProvider): HomeFragmentBinding {
        return HomeFragmentBinding(feature, liveDataProvider)
    }

    @FragmentScope
    @Provides
    fun provideAdapter(): DecksPreviewAdapter {
        return DecksPreviewAdapter()
    }

    @FragmentScope
    @Provides
    fun provideLiveDataProvider(): LiveDataProvider {
        return LiveDataProvider()
    }
}