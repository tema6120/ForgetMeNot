package com.odnovolov.forgetmenot.presentation.screen.home

import com.badoo.mvicore.binder.Binder

class HomeFragmentBindings(
    private val feature: HomeScreenFeature,
    private val recyclerAdapter: DecksPreviewAdapter
) {
    fun setup(fragment: HomeFragment) {
        Binder(fragment.viewLifecycle).run {
            bind(fragment to feature)
            bind(recyclerAdapter to feature)
            bind(feature to fragment)
            bind(feature to recyclerAdapter)
            bind(feature.news to fragment.newsConsumer)
        }
    }
}