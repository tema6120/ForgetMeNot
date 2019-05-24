package com.odnovolov.forgetmenot.presentation.screen.binding

import com.badoo.mvicore.binder.Binder
import com.badoo.mvicore.binder.using
import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature
import com.odnovolov.forgetmenot.presentation.common.adaptForBinder
import com.odnovolov.forgetmenot.presentation.screen.HomeFragment

class HomeFragmentBinding(
    private val addNewDeckFeature: AddNewDeckFeature,
    private val decksPreviewFeature: DecksPreviewFeature
) {
    fun setup(fragment: HomeFragment) {
        val lifecycle = fragment.lifecycle.adaptForBinder()
        val binder = Binder(lifecycle)
        binder.bind(fragment to addNewDeckFeature using UiEventTo.addNewDeckFeatureWish)
        binder.bind(fragment to decksPreviewFeature using UiEventTo.decksPreviewFeatureWish)
    }
}