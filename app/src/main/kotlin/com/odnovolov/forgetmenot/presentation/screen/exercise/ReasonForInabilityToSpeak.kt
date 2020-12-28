package com.odnovolov.forgetmenot.presentation.screen.exercise

import java.util.*

sealed class ReasonForInabilityToSpeak {
    class FailedToInitializeSpeaker(
        val ttsEngine: String?
    ) : ReasonForInabilityToSpeak()

    class LanguageIsNotSupported(
        val ttsEngine: String?,
        val language: Locale
    ) : ReasonForInabilityToSpeak()

    class MissingDataForLanguage(
        val language: Locale
    ) : ReasonForInabilityToSpeak()
}