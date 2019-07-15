package com.odnovolov.forgetmenot.presentation.screen.home

import com.badoo.mvicore.binder.Binder

class DeckSortingBottomSheetBindings(
    private val feature: HomeScreenFeature
) {
    fun setup(bottomSheet: DeckSortingBottomSheet) {
        Binder(bottomSheet.viewLifecycle).run {
            bind(bottomSheet to feature)
            bind(feature to bottomSheet)
            bind(feature.news to bottomSheet.newsConsumer)
        }
    }
}