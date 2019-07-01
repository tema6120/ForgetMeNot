package com.odnovolov.forgetmenot.presentation.screen.exercise

import com.badoo.mvicore.binder.Binder

class ExerciseCardFragmentBindings(
    private val feature: ExerciseScreenFeature
) {
    fun setup(fragment: ExerciseCardFragment) {
        Binder(fragment.binderLifecycle).run {
            bind(fragment to feature)
            bind(feature to fragment)
        }
    }
}