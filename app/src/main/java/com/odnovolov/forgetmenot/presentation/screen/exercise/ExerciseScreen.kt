package com.odnovolov.forgetmenot.presentation.screen.exercise

import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseCard
import com.odnovolov.forgetmenot.presentation.common.Screen
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreen.UiEvent
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreen.UiEvent.NewPageBecomesSelected
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreen.ViewState

class ExerciseScreen : Screen<ViewState, UiEvent, Nothing>(
    initialViewState = ViewState(ArrayList())
) {
    data class ViewState(
        val exerciseCards: List<ExerciseCard>,
        val selectedPagePosition: Int? = null
    )

    sealed class UiEvent {
        data class NewPageBecomesSelected(val position: Int) : UiEvent()
        object ShowAnswerButtonClick : UiEvent()
        object NotAskButtonClick : UiEvent()
        object UndoButtonClick : UiEvent()
    }

    override fun createNewViewStateBasedOnNewUiEvent(uiEvent: UiEvent, viewState: ViewState): ViewState? {
        return when (uiEvent) {
            is NewPageBecomesSelected -> viewState.copy(selectedPagePosition = uiEvent.position)
            else -> null
        }
    }
}