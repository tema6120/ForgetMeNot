package com.odnovolov.forgetmenot.presentation.screen

import com.badoo.mvicore.binder.Binder
import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature
import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature.State
import com.odnovolov.forgetmenot.presentation.common.adaptForBinder
import io.reactivex.functions.Consumer

class HomeFragmentBinding(
    private val feature: AddNewDeckFeature
) {
    fun setup(fragment: HomeFragment) {
        val lifecycle = fragment.lifecycle.adaptForBinder()
        val binder = Binder(lifecycle)
        binder.bind(fragment to feature)
        binder.bind(feature to Consumer<State>(fragment::render))
    }
}