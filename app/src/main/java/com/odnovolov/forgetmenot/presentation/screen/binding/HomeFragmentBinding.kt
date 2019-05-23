package com.odnovolov.forgetmenot.presentation.screen.binding

import com.badoo.mvicore.binder.Binder
import com.badoo.mvicore.binder.using
import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature
import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature.State
import com.odnovolov.forgetmenot.presentation.common.adaptForBinder
import com.odnovolov.forgetmenot.presentation.screen.HomeFragment
import io.reactivex.functions.Consumer

class HomeFragmentBinding(
    private val feature: AddNewDeckFeature
) {
    fun setup(fragment: HomeFragment) {
        val lifecycle = fragment.lifecycle.adaptForBinder()
        val binder = Binder(lifecycle)
        binder.bind(fragment to feature using UiEventToWish())
        binder.bind(feature to Consumer<State>(fragment::render))
    }
}