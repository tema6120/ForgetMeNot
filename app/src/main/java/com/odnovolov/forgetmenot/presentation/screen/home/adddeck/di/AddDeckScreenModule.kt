package com.odnovolov.forgetmenot.presentation.screen.home.adddeck.di

import com.badoo.mvicore.android.AndroidTimeCapsule
import com.odnovolov.forgetmenot.domain.feature.adddeck.AddDeckFeature
import com.odnovolov.forgetmenot.domain.feature.decksexplorer.DecksExplorerFeature
import com.odnovolov.forgetmenot.presentation.di.fragmentscope.FragmentScope
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckFragmentBindings
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenFeature
import dagger.Module
import dagger.Provides

@Module
class AddDeckScreenModule {

    @FragmentScope
    @Provides
    fun addDeckScreenFeature(
        timeCapsule: AndroidTimeCapsule,
        addDeckFeature: AddDeckFeature,
        decksExplorerFeature: DecksExplorerFeature
    ) = AddDeckScreenFeature(
        timeCapsule,
        addDeckFeature,
        decksExplorerFeature
    )

    @FragmentScope
    @Provides
    fun addDeckFragmentBindings(
        addDeckScreenFeature: AddDeckScreenFeature
    ) = AddDeckFragmentBindings(
        addDeckScreenFeature
    )
}