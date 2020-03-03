package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.RegistrableFlowableState
import java.util.*

class Pronunciation(
    override val id: Long,
    name: String,
    questionLanguage: Locale?,
    questionAutoSpeak: Boolean,
    answerLanguage: Locale?,
    answerAutoSpeak: Boolean,
    doNotSpeakTextInBrackets: Boolean
) : RegistrableFlowableState<Pronunciation>() {
    var name: String by me(name)
    var questionLanguage: Locale? by me(questionLanguage)
    var questionAutoSpeak: Boolean by me(questionAutoSpeak)
    var answerLanguage: Locale? by me(answerLanguage)
    var answerAutoSpeak: Boolean by me(answerAutoSpeak)
    var doNotSpeakTextInBrackets: Boolean by me(doNotSpeakTextInBrackets)

    override fun copy() = Pronunciation(
        id,
        name,
        questionLanguage,
        questionAutoSpeak,
        answerLanguage,
        answerAutoSpeak,
        doNotSpeakTextInBrackets
    )

    companion object {
        val Default by lazy {
            Pronunciation(
                id = 0L,
                name = "",
                questionLanguage = null,
                questionAutoSpeak = false,
                answerLanguage = null,
                answerAutoSpeak = false,
                doNotSpeakTextInBrackets = false
            )
        }
    }
}