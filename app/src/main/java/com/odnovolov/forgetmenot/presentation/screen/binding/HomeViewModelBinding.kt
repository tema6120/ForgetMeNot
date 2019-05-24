package com.odnovolov.forgetmenot.presentation.screen.binding

import com.badoo.mvicore.binder.Binder
import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature
import com.odnovolov.forgetmenot.presentation.screen.HomeViewModel

class HomeViewModelBinding(
    private val feature: AddNewDeckFeature,
    private val liveDataProvider: LiveDataProvider
) {
    fun setup(viewModel: HomeViewModel) {
        val binder = Binder(viewModel.lifecycle)
        binder.bind(feature to liveDataProvider.stateConsumer)
    }
}