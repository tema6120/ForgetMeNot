package com.odnovolov.forgetmenot.presentation.screen.exercise

import com.badoo.mvicore.binder.Binder
import com.badoo.mvicore.binder.using
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseFeature
import com.odnovolov.forgetmenot.presentation.common.LifecycleScope.START_STOP
import com.odnovolov.forgetmenot.presentation.common.adaptForBinder

class ExerciseFragmentBindings(
    private val feature: ExerciseFeature
) {
    fun setup(fragment: ExerciseFragment) {
        Binder(fragment.lifecycle.adaptForBinder(START_STOP)).run {
            bind(feature to fragment using ViewStateAdapter)
        }
    }

    object ViewStateAdapter : (ExerciseFeature.State) -> ExerciseFragment.ViewState? {
        override fun invoke(featureState: ExerciseFeature.State): ExerciseFragment.ViewState? {
            return ExerciseFragment.ViewState(
                featureState.exerciseData.exerciseCards
            )
        }

    }
}