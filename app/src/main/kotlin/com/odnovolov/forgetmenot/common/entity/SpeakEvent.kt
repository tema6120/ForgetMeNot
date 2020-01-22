package com.odnovolov.forgetmenot.common.entity

sealed class SpeakEvent {
    object SpeakQuestion : SpeakEvent()
    object SpeakAnswer : SpeakEvent()
    data class Delay(val seconds: Int) : SpeakEvent()
}