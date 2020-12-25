package com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.motivationaltimer

sealed class MotivationalTimerEvent {
    object TimeForAnswerSwitchToggled : MotivationalTimerEvent()
    class TimeInputChanged(val text: String) : MotivationalTimerEvent()
    object OkButtonClicked : MotivationalTimerEvent()
}