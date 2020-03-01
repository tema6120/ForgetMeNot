package com.odnovolov.forgetmenot.domain.entity

sealed class SpeakEvent {
    object SpeakQuestion : SpeakEvent()
    object SpeakAnswer : SpeakEvent()
    data class Delay(val seconds: Int) : SpeakEvent()
}