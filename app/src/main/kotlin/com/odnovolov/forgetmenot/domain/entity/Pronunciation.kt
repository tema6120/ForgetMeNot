package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import java.util.*

class Pronunciation(
    override val id: Long,
    name: String,
    questionLanguage: Locale?,
    questionAutoSpeak: Boolean,
    answerLanguage: Locale?,
    answerAutoSpeak: Boolean,
    speakTextInBrackets: Boolean
) : FlowMakerWithRegistry<Pronunciation>() {
    var name: String by flowMaker(name)
    var questionLanguage: Locale? by flowMaker(questionLanguage)
    var questionAutoSpeak: Boolean by flowMaker(questionAutoSpeak)
    var answerLanguage: Locale? by flowMaker(answerLanguage)
    var answerAutoSpeak: Boolean by flowMaker(answerAutoSpeak)
    var speakTextInBrackets: Boolean by flowMaker(speakTextInBrackets)

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
                speakTextInBrackets = false
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