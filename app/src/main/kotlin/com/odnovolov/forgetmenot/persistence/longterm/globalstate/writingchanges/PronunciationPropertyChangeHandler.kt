package com.odnovolov.forgetmenot.persistence.longterm.globalstate.writingchanges

import com.odnovolov.forgetmenot.persistence.database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.entity.Pronunciation
import java.util.*

object PronunciationPropertyChangeHandler {
    private val queries = database.pronunciationQueries

    fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        val pronunciationId = change.propertyOwnerId
        if (!queries.exists(pronunciationId).executeAsOne()) return
        when (change.property) {
            Pronunciation::name -> {
                val name = change.newValue as String
                queries.updateName(name, pronunciationId)
            }
            Pronunciation::questionLanguage -> {
                val questionLanguage = change.newValue as Locale?
                queries.updateQuestionLanguage(questionLanguage, pronunciationId)
            }
            Pronunciation::questionAutoSpeak -> {
                val questionAutoSpeak = change.newValue as Boolean
                queries.updateQuestionAutoSpeak(questionAutoSpeak, pronunciationId)
            }
            Pronunciation::answerLanguage -> {
                val answerLanguage = change.newValue as Locale?
                queries.updateAnswerLanguage(answerLanguage, pronunciationId)
            }
            Pronunciation::answerAutoSpeak -> {
                val answerAutoSpeak = change.newValue as Boolean
                queries.updateAnswerAutoSpeak(answerAutoSpeak, pronunciationId)
            }
            Pronunciation::speakTextInBrackets -> {
                val speakTextInBrackets = change.newValue as Boolean
                queries.updateSpeakTextInBrackets(speakTextInBrackets, pronunciationId)
            }
        }
    }
}