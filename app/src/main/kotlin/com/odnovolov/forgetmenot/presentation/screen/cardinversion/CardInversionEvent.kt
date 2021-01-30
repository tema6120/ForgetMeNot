package com.odnovolov.forgetmenot.presentation.screen.cardinversion

sealed class CardInversionEvent {
    object CloseTipButtonClicked : CardInversionEvent()
    object OffRadioButtonClicked : CardInversionEvent()
    object OnRadioButtonClicked : CardInversionEvent()
    object EveryOtherLapRadioButtonClicked : CardInversionEvent()
    object RandomlyButtonClicked : CardInversionEvent()
}