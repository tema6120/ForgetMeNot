package com.odnovolov.forgetmenot.presentation.screen.example

sealed class ExampleExerciseEvent {
    object BottomSheetExpanded : ExampleExerciseEvent()
    object BottomSheetCollapsed : ExampleExerciseEvent()
    class PageSelected(val position: Int) : ExampleExerciseEvent()
    object SpeakButtonClicked : ExampleExerciseEvent()
    object StopSpeakButtonClicked : ExampleExerciseEvent()
    object StopTimerButtonClicked : ExampleExerciseEvent()
    object FragmentResumed : ExampleExerciseEvent()
    object FragmentPaused : ExampleExerciseEvent()
}