package com.odnovolov.forgetmenot.domain.entity

import com.soywiz.klock.TimeSpan

sealed class PronunciationEvent {
    object SpeakQuestion : PronunciationEvent()
    object SpeakAnswer : PronunciationEvent()
    data class Delay(val timeSpan: TimeSpan) : PronunciationEvent()
}