package com.odnovolov.forgetmenot.domain.entity

import com.soywiz.klock.TimeSpan

sealed class SpeakEvent {
    object SpeakQuestion : SpeakEvent()
    object SpeakAnswer : SpeakEvent()
    data class Delay(val timeSpan: TimeSpan) : SpeakEvent()
}