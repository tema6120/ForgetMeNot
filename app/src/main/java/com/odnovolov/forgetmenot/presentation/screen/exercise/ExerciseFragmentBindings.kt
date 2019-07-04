package com.odnovolov.forgetmenot.presentation.screen.exercise

import com.badoo.mvicore.binder.Binder

class ExerciseFragmentBindings(
    feature: ExerciseScreenFeature,
    fragment: ExerciseFragment,
    viewPagerAdapter: ExerciseCardsAdapter
) {
    private val binder = Binder(fragment.viewLifecycle).apply {
        bind(fragment to feature)
        bind(feature to fragment)
        bind(feature to viewPagerAdapter)
        bind(feature.news to fragment.newsConsumer)
    }
}