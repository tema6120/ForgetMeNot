package com.odnovolov.forgetmenot.presentation.screen.player.view.laps

sealed class LapsInPlayerEvent {
    object LapsRadioButtonClicked : LapsInPlayerEvent()
    class LapsInputChanged(val numberOfLapsInput: String) : LapsInPlayerEvent()
    object InfinitelyRadioButtonClicked : LapsInPlayerEvent()
    object OkButtonClicked : LapsInPlayerEvent()
}