package com.odnovolov.forgetmenot.presentation.di.fragmentscope

import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature
import com.odnovolov.forgetmenot.presentation.navigation.Navigator
import com.odnovolov.forgetmenot.presentation.screen.home.binding.HomeFragmentBinding
import dagger.Module
import dagger.Provides

@Module
class HomeFragmentModule {

    @FragmentScope
    @Provides
    fun provideBinding(
        addNewDeckFeature: AddNewDeckFeature,
        decksPreviewFeature: DecksPreviewFeature,
        navigator: Navigator
    ): HomeFragmentBinding {
        return HomeFragmentBinding(addNewDeckFeature, decksPreviewFeature, navigator)
    }
}