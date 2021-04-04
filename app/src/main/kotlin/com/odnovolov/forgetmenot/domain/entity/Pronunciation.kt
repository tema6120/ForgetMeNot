package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import java.util.*

class Pronunciation(
    override val id: Long,
    questionLanguage: Locale?,
    questionAutoSpeaking: Boolean,
    answerLanguage: Locale?,
    answerAutoSpeaking: Boolean,
    speakTextInBrackets: Boolean
) : FlowMakerWithRegistry<Pronunciation>() {
    var questionLanguage: Locale? by flowMaker(questionLanguage)
    var questionAutoSpeaking: Boolean by flowMaker(questionAutoSpeaking)
    var answerLanguage: Locale? by flowMaker(answerLanguage)
    var answerAutoSpeaking: Boolean by flowMaker(answerAutoSpeaking)
    var speakTextInBrackets: Boolean by flowMaker(speakTextInBrackets)

    override fun copy() = Pronunciation(
        id,
        questionLanguage,
        questionAutoSpeaking,
        answerLanguage,
        answerAutoSpeaking,
        speakTextInBrackets
    )

    companion object {
        val Default = Pronunciation(
            id = 0L,
            questionLanguage = null,
            questionAutoSpeaking = false,
            answerLanguage = null,
            answerAutoSpeaking = false,
            speakTextInBrackets = false
        )
    }
}

fun Pronunciation.isDefault(): Boolean = id == Pronunciation.Default.id