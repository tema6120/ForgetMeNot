package com.odnovolov.forgetmenot.presentation.screen.decksettings.motivationaltimer

sealed class MotivationalTimerEvent {
    object TimeForAnswerSwitchToggled : MotivationalTimerEvent()
    class TimeInputChanged(val text: String) : MotivationalTimerEvent()
    object OkButtonClicked : MotivationalTimerEvent()
}