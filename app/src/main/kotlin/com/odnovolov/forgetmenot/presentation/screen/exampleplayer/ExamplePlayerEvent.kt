package com.odnovolov.forgetmenot.presentation.screen.exampleplayer

sealed class ExamplePlayerEvent {
    object BottomSheetExpanded : ExamplePlayerEvent()
    object BottomSheetCollapsed : ExamplePlayerEvent()
    class PageWasChanged(val position: Int) : ExamplePlayerEvent()
    object SpeakButtonClicked : ExamplePlayerEvent()
    object StopSpeakButtonClicked : ExamplePlayerEvent()
    object PauseButtonClicked : ExamplePlayerEvent()
    object ResumeButtonClicked : ExamplePlayerEvent()
    object FragmentPaused : ExamplePlayerEvent()
}