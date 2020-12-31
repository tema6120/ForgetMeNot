package com.odnovolov.forgetmenot.presentation.screen.motivationaltimer

sealed class MotivationalTimerEvent {
    object HelpButtonClicked : MotivationalTimerEvent()
    object TimeForAnswerSwitchToggled : MotivationalTimerEvent()
    class TimeInputChanged(val text: String) : MotivationalTimerEvent()
    object OkButtonClicked : MotivationalTimerEvent()
}