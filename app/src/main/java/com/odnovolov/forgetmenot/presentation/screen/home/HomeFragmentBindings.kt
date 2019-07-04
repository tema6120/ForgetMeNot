package com.odnovolov.forgetmenot.presentation.screen.home

import com.badoo.mvicore.binder.Binder
import com.odnovolov.forgetmenot.presentation.common.LifecycleScope.CREATE_DESTROY
import com.odnovolov.forgetmenot.presentation.common.adaptForBinder

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