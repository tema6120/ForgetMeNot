package com.odnovolov.forgetmenot.presentation.screen.home.binding

import com.badoo.mvicore.binder.Binder
import com.badoo.mvicore.binder.using
import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature
import com.odnovolov.forgetmenot.presentation.common.adaptForBinder
import com.odnovolov.forgetmenot.presentation.navigation.NavigationEventFinder
import com.odnovolov.forgetmenot.presentation.navigation.Navigator
import com.odnovolov.forgetmenot.presentation.screen.home.HomeFragment

class HomeFragmentBinding(
    private val addNewDeckFeature: AddNewDeckFeature,
    private val decksPreviewFeature: DecksPreviewFeature,
    private val navigator: Navigator
) {
    fun setup(fragment: HomeFragment) {
        val lifecycle = fragment.lifecycle.adaptForBinder()
        Binder(lifecycle).run {
            bind(fragment to addNewDeckFeature using UiEventTo.addNewDeckFeatureWish)
            bind(fragment to decksPreviewFeature using UiEventTo.decksPreviewFeatureWish)
            bind(decksPreviewFeature.news to navigator using NavigationEventFinder.fromDecksPreviewFeature)
        }
    }
}