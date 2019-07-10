package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import com.badoo.mvicore.binder.Binder

class AddDeckFragmentBindings(
    private val feature: AddDeckScreenFeature
) {
    fun setup(fragment: AddDeckFragment) {
        Binder(fragment.viewLifecycle).run {
            bind(feature to fragment)
            bind(feature.news to fragment.newsConsumer)
            bind(fragment to feature)
        }
    }
}