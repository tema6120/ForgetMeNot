package com.odnovolov.forgetmenot.presentation.screen.cardfilterforexercise.cardlimit

sealed class CardLimitEvent {
    object LimitRadioButtonClicked : CardLimitEvent()
    class DialogTextChanged(val text: String) : CardLimitEvent()
    object NoLimitRadioButtonClicked : CardLimitEvent()
    object OkButtonClicked : CardLimitEvent()
}