package com.odnovolov.forgetmenot.domain.entity

import java.util.*

interface Speaker {
    fun speak(text: String, language: Locale?)
}