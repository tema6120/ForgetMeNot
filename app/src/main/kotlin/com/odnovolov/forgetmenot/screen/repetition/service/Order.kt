package com.odnovolov.forgetmenot.screen.repetition.service

import java.util.*

sealed class RepetitionServiceOrder {
    class Speak(val text: String, val language: Locale?) : RepetitionServiceOrder()
    object StopSpeaking : RepetitionServiceOrder()
}