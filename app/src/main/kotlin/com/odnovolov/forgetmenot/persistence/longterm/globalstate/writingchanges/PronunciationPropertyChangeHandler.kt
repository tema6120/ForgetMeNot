package com.odnovolov.forgetmenot.persistence.longterm.globalstate.writingchanges

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.entity.Pronunciation
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler
import java.util.*

class PronunciationPropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.pronunciationQueries

    override fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        val pronunciationId: Long = change.propertyOwnerId
        val exists: Boolean = queries.exists(pronunciationId).executeAsOne()
        if (!exists) return
        when (change.property) {
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