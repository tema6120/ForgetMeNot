package com.odnovolov.forgetmenot.presentation.screen.home.di

import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature
import com.odnovolov.forgetmenot.presentation.di.fragmentscope.FragmentScope
import com.odnovolov.forgetmenot.presentation.screen.home.DecksPreviewAdapter
import com.odnovolov.forgetmenot.presentation.screen.home.HomeFragmentBindings
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreen
import dagger.Module
import dagger.Provides

@Module
class HomeScreenModule {

    @FragmentScope
    @Provides
    fun homeScreen(): HomeScreen {
        return HomeScreen()
    }

    @FragmentScope
    @Provides
    fun decksPreviewAdapter(): DecksPreviewAdapter {
        return DecksPreviewAdapter()
    }

    @FragmentScope
    @Provides
    fun homeFragmentBindings(
        addNewDeckFeature: AddNewDeckFeature,
        decksPreviewFeature: DecksPreviewFeature,
        homeScreen: HomeScreen,
        decksPreviewAdapter: DecksPreviewAdapter
    ): HomeFragmentBindings {
        return HomeFragmentBindings(
            addNewDeckFeature,
            decksPreviewFeature,
            homeScreen,
            decksPreviewAdapter
        )
    }
}