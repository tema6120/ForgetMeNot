package com.odnovolov.forgetmenot.domain.entity

import com.soywiz.klock.TimeSpan

sealed class SpeakEvent {
    abstract val id: Long
    data class SpeakQuestion(override val id: Long) : SpeakEvent()
    data class SpeakAnswer(override val id: Long) : SpeakEvent()
    data class Delay(override val id: Long, val timeSpan: TimeSpan) : SpeakEvent()
}