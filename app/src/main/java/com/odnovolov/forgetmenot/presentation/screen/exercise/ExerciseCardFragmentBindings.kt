package com.odnovolov.forgetmenot.presentation.screen.exercise

import com.badoo.mvicore.binder.Binder
import com.odnovolov.forgetmenot.presentation.common.LifecycleScope.START_STOP
import com.odnovolov.forgetmenot.presentation.common.adaptForBinder

class ExerciseCardFragmentBindings(
    private val screen: ExerciseScreen
) {
    fun setup(fragment: ExerciseCardFragment) {
        Binder(fragment.lifecycle.adaptForBinder(START_STOP)).run {
            bind(screen.viewState to fragment)
            bind(fragment to screen.uiEventConsumer)
        }
    }
}