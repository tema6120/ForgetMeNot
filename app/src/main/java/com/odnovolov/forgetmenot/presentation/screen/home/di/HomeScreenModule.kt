package com.odnovolov.forgetmenot.presentation.screen.home.di

import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature
import com.odnovolov.forgetmenot.presentation.di.fragmentscope.FragmentScope
import com.odnovolov.forgetmenot.presentation.screen.home.HomeFragmentBindings
import dagger.Module
import dagger.Provides

@Module
class HomeScreenModule {

    @FragmentScope
    @Provides
    fun provideBindings(
        addNewDeckFeature: AddNewDeckFeature,
        decksPreviewFeature: DecksPreviewFeature
    ): HomeFragmentBindings {
        return HomeFragmentBindings(addNewDeckFeature, decksPreviewFeature)
    }
}