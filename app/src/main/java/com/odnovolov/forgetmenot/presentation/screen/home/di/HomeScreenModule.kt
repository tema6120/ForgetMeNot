package com.odnovolov.forgetmenot.presentation.screen.home.di

import com.badoo.mvicore.android.AndroidTimeCapsule
import com.odnovolov.forgetmenot.domain.feature.decksexplorer.DecksExplorerFeature
import com.odnovolov.forgetmenot.domain.feature.deletedeck.DeleteDeckFeature
import com.odnovolov.forgetmenot.domain.feature.exercisecreator.ExerciseCreatorFeature
import com.odnovolov.forgetmenot.presentation.di.fragmentscope.FragmentScope
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSortingBottomSheetBindings
import com.odnovolov.forgetmenot.presentation.screen.home.DecksPreviewAdapter
import com.odnovolov.forgetmenot.presentation.screen.home.HomeFragmentBindings
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature
import dagger.Module
import dagger.Provides

@Module
class HomeScreenModule {

    @FragmentScope
    @Provides
    fun homeScreenFeature(
        timeCapsule: AndroidTimeCapsule,
        decksExplorerFeature: DecksExplorerFeature,
        deleteDeckFeature: DeleteDeckFeature,
        exerciseCreatorFeature: ExerciseCreatorFeature
    ) = HomeScreenFeature(
        timeCapsule,
        decksExplorerFeature,
        deleteDeckFeature,
        exerciseCreatorFeature
    )

    @FragmentScope
    @Provides
    fun decksPreviewAdapter() = DecksPreviewAdapter()

    @FragmentScope
    @Provides
    fun homeFragmentBindings(
        homeScreenFeature: HomeScreenFeature,
        decksPreviewAdapter: DecksPreviewAdapter
    ) = HomeFragmentBindings(
        homeScreenFeature,
        decksPreviewAdapter
    )

    @Provides
    fun deckSortingBottomSheetBindings(
        homeScreenFeature: HomeScreenFeature
    ) = DeckSortingBottomSheetBindings(
        homeScreenFeature
    )
}