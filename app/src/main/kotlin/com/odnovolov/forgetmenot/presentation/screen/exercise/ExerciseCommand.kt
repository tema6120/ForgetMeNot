package com.odnovolov.forgetmenot.presentation.screen.exercise

sealed class ExerciseCommand {
    object MoveToNextPosition : ExerciseCommand()
    object MoveToPreviousPosition : ExerciseCommand()
    object ShowChooseHintPopup : ExerciseCommand()
    class ShowLevelOfKnowledgePopup(val intervalItems: List<IntervalItem>) : ExerciseCommand()
    object ShowIntervalsAreOffMessage : ExerciseCommand()
}