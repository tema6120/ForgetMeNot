package com.odnovolov.forgetmenot.screen.exercise

import java.util.*

sealed class ExerciseOrder {
    object MoveToNextPosition : ExerciseOrder()
    class Speak(val text: String, val language: Locale?) : ExerciseOrder()
    object NavigateToEditCard : ExerciseOrder()
    object ShowChooseHintPopup : ExerciseOrder()
    class ShowLevelOfKnowledgePopup(val intervalItems: List<IntervalItem>) : ExerciseOrder()
    object ShowIntervalsAreOffMessage : ExerciseOrder()
}