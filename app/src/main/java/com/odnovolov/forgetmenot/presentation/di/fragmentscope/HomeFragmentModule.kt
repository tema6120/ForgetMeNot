package com.odnovolov.forgetmenot.presentation.di.fragmentscope

import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature
import com.odnovolov.forgetmenot.presentation.screen.DecksPreviewAdapter
import com.odnovolov.forgetmenot.presentation.screen.binding.HomeFragmentBinding
import dagger.Module
import dagger.Provides

@Module
class HomeFragmentModule {

    @FragmentScope
    @Provides
    fun provideBinding(feature1: AddNewDeckFeature, feature2: DecksPreviewFeature): HomeFragmentBinding {
        return HomeFragmentBinding(feature1, feature2)
    }

    @FragmentScope
    @Provides
    fun provideAdapter(): DecksPreviewAdapter {
        return DecksPreviewAdapter()
    }
}