package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.laps

sealed class RepetitionLapsEvent {
    object LapsRadioButtonClicked : RepetitionLapsEvent()
    class LapsInputChanged(val numberOfLapsInput: String) : RepetitionLapsEvent()
    object InfinitelyRadioButtonClicked : RepetitionLapsEvent()
    object OkButtonClicked : RepetitionLapsEvent()
}