package com.odnovolov.forgetmenot.entity

import java.util.*

data class Pronunciation(
    val id: Int = 0,
    val name: String,
    val questionLanguage: Locale? = null,
    val questionAutoSpeak: Boolean = false,
    val answerLanguage: Locale? = null,
    val answerAutoSpeak: Boolean = false
)