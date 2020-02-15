package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.Copyable
import com.odnovolov.forgetmenot.domain.architecturecomponents.RegistrableFlowableState
import java.util.*

class Pronunciation(
    override val id: Long = 0,
    name: String = "",
    questionLanguage: Locale? = null,
    questionAutoSpeak: Boolean = false,
    answerLanguage: Locale? = null,
    answerAutoSpeak: Boolean = false,
    doNotSpeakTextInBrackets: Boolean = false
) : RegistrableFlowableState<Pronunciation>(), Copyable {
    var name: String by me(name)
    var questionLanguage: Locale? by me(questionLanguage)
    var questionAutoSpeak: Boolean by me(questionAutoSpeak)
    var answerLanguage: Locale? by me(answerLanguage)
    var answerAutoSpeak: Boolean by me(answerAutoSpeak)
    var doNotSpeakTextInBrackets: Boolean by me(doNotSpeakTextInBrackets)

    override fun copy() = Pronunciation(
        id, name, questionLanguage, questionAutoSpeak,
        answerLanguage, answerAutoSpeak, doNotSpeakTextInBrackets
    )

    companion object {
        val Default by lazy { Pronunciation() }
    }
}