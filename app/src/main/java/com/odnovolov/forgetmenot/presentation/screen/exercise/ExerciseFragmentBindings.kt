package com.odnovolov.forgetmenot.presentation.screen.exercise

import com.badoo.mvicore.binder.Binder
import com.badoo.mvicore.binder.using
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseFeature
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseFeature.Wish
import com.odnovolov.forgetmenot.presentation.common.LifecycleScope.START_STOP
import com.odnovolov.forgetmenot.presentation.common.UiEventWitViewState
import com.odnovolov.forgetmenot.presentation.common.adaptForBinder
import com.odnovolov.forgetmenot.presentation.common.withLatest
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreen.UiEvent
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreen.UiEvent.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreen.ViewState

class ExerciseFragmentBindings(
    private val feature: ExerciseFeature,
    private val screen: ExerciseScreen,
    private val viewPagerAdapter: ExerciseCardsAdapter
) {
    fun setup(fragment: ExerciseFragment) {
        Binder(fragment.lifecycle.adaptForBinder(START_STOP)).run {
            bind(fragment to screen.uiEventConsumer)
            bind(screen.uiEventWithLatestViewState to feature using UiEventToWish)
            bind(feature.withLatest(screen.viewState) to screen.viewStateConsumer using ViewStateAdapter)
            bind(screen.viewState to fragment)
            bind(screen.viewState to viewPagerAdapter)
            bind(screen.news to fragment.newsConsumer)
        }
    }

    object UiEventToWish : (UiEventWitViewState<UiEvent, ViewState>) -> Wish? {
        override fun invoke(uewvs: UiEventWitViewState<UiEvent, ViewState>): Wish? {
            val (uiEvent, viewState) = uewvs
            val currentPosition = viewState.currentPosition ?: return null
            return when (uiEvent) {
                is ShowAnswerButtonClick -> Wish.ShowAnswer(currentPosition)
                is NotAskButtonClick -> Wish.SetCardAsLearned(currentPosition)
                is UndoButtonClick -> Wish.SetCardAsUnlearned(currentPosition)
                else -> null
            }
        }
    }

    object ViewStateAdapter : (Pair<ExerciseFeature.State, ViewState>) -> ViewState? {
        override fun invoke(pair: Pair<ExerciseFeature.State, ViewState>): ViewState? {
            val (featureState, viewState) = pair
            return viewState.copy(exerciseCards = featureState.exerciseData.exerciseCards)
        }
    }
}