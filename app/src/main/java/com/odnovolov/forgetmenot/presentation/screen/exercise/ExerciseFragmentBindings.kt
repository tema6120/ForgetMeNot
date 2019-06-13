package com.odnovolov.forgetmenot.presentation.screen.exercise

import android.util.Log
import com.badoo.mvicore.binder.Binder
import com.badoo.mvicore.binder.using
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseFeature
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseFeature.State
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseFeature.Wish
import com.odnovolov.forgetmenot.presentation.common.LifecycleScope.START_STOP
import com.odnovolov.forgetmenot.presentation.common.adaptForBinder
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseFragment.UiEvent
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseFragment.ViewState

class ExerciseFragmentBindings(
    private val feature: ExerciseFeature
) {
    fun setup(fragment: ExerciseFragment) {
        Binder(fragment.lifecycle.adaptForBinder(START_STOP)).run {
            bind(feature to fragment using ViewStateAdapter)
            bind(fragment to feature using UiEventToWish)
        }
    }

    object ViewStateAdapter : (State) -> ViewState? {
        override fun invoke(featureState: State): ViewState? {
            return ViewState(
                featureState.exerciseData.exerciseCards
            )
        }
    }

    object UiEventToWish : (UiEvent) -> Wish? {
        override fun invoke(uiEvent: UiEvent): Wish? {
            Log.d("odnovolov", "get Event $uiEvent")
            return null
        }
    }
}