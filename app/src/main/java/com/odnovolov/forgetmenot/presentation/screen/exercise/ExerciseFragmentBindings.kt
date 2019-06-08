package com.odnovolov.forgetmenot.presentation.screen.exercise

import com.badoo.mvicore.binder.Binder
import com.badoo.mvicore.binder.using
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseFeature
import com.odnovolov.forgetmenot.presentation.common.adaptForBinder

class ExerciseFragmentBindings(
    private val feature: ExerciseFeature
) {
    fun setup(fragment: ExerciseFragment) {
        val lifecycle = fragment.lifecycle.adaptForBinder()
        Binder(lifecycle).run {
            bind(feature to fragment using ::convert)
        }
    }

    private fun convert(featureState: ExerciseFeature.State): ExerciseFragment.ViewState {
        return ExerciseFragment.ViewState(
            featureState.exerciseData.exerciseCards
        )
    }
}