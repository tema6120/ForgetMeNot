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
    speakTextInBrackets: Boolean
) : RegistrableFlowableState<Pronunciation>() {
    var name: String by me(name)
    var questionLanguage: Locale? by me(questionLanguage)
    var questionAutoSpeak: Boolean by me(questionAutoSpeak)
    var answerLanguage: Locale? by me(answerLanguage)
    var answerAutoSpeak: Boolean by me(answerAutoSpeak)
    var speakTextInBrackets: Boolean by me(speakTextInBrackets)

    override fun copy() = Pronunciation(
        id,
        name,
        questionLanguage,
        questionAutoSpeak,
        answerLanguage,
        answerAutoSpeak,
        speakTextInBrackets
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
                speakTextInBrackets = true
            )
        }
    }
}

fun Pronunciation.isDefault(): Boolean = id == Pronunciation.Default.id

fun Pronunciation.isIndividual(): Boolean = !isDefault() && name.isEmpty()

fun checkPronunciationName(testedName: String, globalState: GlobalState): NameCheckResult {
    return when {
        testedName.isEmpty() -> NameCheckResult.Empty
        globalState.sharedPronunciations.any { it.name == testedName } -> NameCheckResult.Occupied
        else -> NameCheckResult.Ok
    }
}