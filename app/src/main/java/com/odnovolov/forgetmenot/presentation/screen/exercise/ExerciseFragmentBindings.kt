package com.odnovolov.forgetmenot.presentation.screen.exercise

import com.badoo.mvicore.binder.Binder
import com.badoo.mvicore.binder.using
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseFeature
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseFeature.Wish
import com.odnovolov.forgetmenot.presentation.common.FeatureStateWithViewState
import com.odnovolov.forgetmenot.presentation.common.LifecycleScope.START_STOP
import com.odnovolov.forgetmenot.presentation.common.UiEventWitViewState
import com.odnovolov.forgetmenot.presentation.common.adaptForBinder
import com.odnovolov.forgetmenot.presentation.common.withLatest
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreen.UiEvent
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreen.UiEvent.ShowAnswerButtonClick
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreen.ViewState

class ExerciseFragmentBindings(
    private val feature: ExerciseFeature,
    private val screen: ExerciseScreen,
    private val viewPagerAdapter: ExerciseCardsAdapter
) {
    fun setup(fragment: ExerciseFragment) {
        Binder(fragment.lifecycle.adaptForBinder(START_STOP)).run {
            bind(fragment to screen.uiEventConsumer)
            bind(viewPagerAdapter.uiEventEmitter to screen.uiEventConsumer)
            bind(screen.uiEventWithLatestViewState to feature using UiEventToWish)
            bind(feature.withLatest(screen.viewState) to screen.viewStateConsumer using ViewStateAdapter)
            bind(screen.viewState to fragment)
        }
    }

    object UiEventToWish : (UiEventWitViewState<UiEvent, ViewState>) -> Wish? {
        override fun invoke(uewvs: UiEventWitViewState<UiEvent, ViewState>): Wish? {
            val (uiEvent, viewState) = uewvs
            return when (uiEvent) {
                is ShowAnswerButtonClick -> viewState.selectedPagePosition?.let { Wish.ShowAnswer(it) }
                else -> null
            }
        }
    }

    object ViewStateAdapter : (FeatureStateWithViewState<ExerciseFeature.State, ViewState>) -> ViewState? {
        override fun invoke(swvs: FeatureStateWithViewState<ExerciseFeature.State, ViewState>): ViewState? {
            val (featureState, viewState) = swvs
            return viewState.copy(exerciseCards = featureState.exerciseData.exerciseCards)
        }
    }
}