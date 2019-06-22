package com.odnovolov.forgetmenot.presentation.screen.exercise

import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseCard
import com.odnovolov.forgetmenot.presentation.common.Screen
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreen.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreen.UiEvent.NewPageBecomesSelected

class ExerciseScreen : Screen<ViewState, UiEvent, News>(
    initialViewState = ViewState(ArrayList())
) {
    data class ViewState(
        val exerciseCards: List<ExerciseCard>,
        val currentPosition: Int? = null
    )

    sealed class UiEvent {
        data class NewPageBecomesSelected(val position: Int) : UiEvent()
        object ShowAnswerButtonClick : UiEvent()
        object NotAskButtonClick : UiEvent()
        object UndoButtonClick : UiEvent()
    }

    sealed class News {
        object MoveToNextPosition : News()
    }

    override fun updateViewStateOnUiEvent(uiEvent: UiEvent, viewState: ViewState): ViewState? {
        return when (uiEvent) {
            is NewPageBecomesSelected -> viewState.copy(currentPosition = uiEvent.position)
            else -> null
        }
    }

    override fun generateNewsOnViewStateUpdate(oldViewState: ViewState, newViewState: ViewState): News? {
        if (oldViewState.currentPosition == null || isLastPosition(oldViewState)) {
            return null
        }
        if (oldViewState.currentPosition == newViewState.currentPosition) {
            val currentPosition = oldViewState.currentPosition
            val oldIsLearned = oldViewState.exerciseCards[currentPosition].card.isLearned
            val newIsLearned = newViewState.exerciseCards[currentPosition].card.isLearned
            if (!oldIsLearned && newIsLearned) {
                return News.MoveToNextPosition
            }
        }
        return null
    }

    private fun isLastPosition(viewState: ViewState): Boolean {
        val (exerciseCards, currentPosition) = viewState
        val lastPosition = exerciseCards.size - 1
        return currentPosition == lastPosition
    }
}