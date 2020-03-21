package com.odnovolov.forgetmenot.persistence.longterm.globalstate.writingchanges

import com.odnovolov.forgetmenot.persistence.database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.entity.Card
import com.soywiz.klock.DateTime

object CardPropertyChangeHandler {
    private val queries = database.cardQueries

    fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        val cardId = change.propertyOwnerId
        if (!queries.exists(cardId).executeAsOne()) return
        when (change.property) {
            Card::question -> {
                val question = change.newValue as String
                queries.updateQuestion(question, cardId)
            }
            Card::answer -> {
                val answer = change.newValue as String
                queries.updateAnswer(answer, cardId)
            }
            Card::lap -> {
                val lap = change.newValue as Int
                queries.updateLap(lap, cardId)
            }
            Card::isLearned -> {
                val isLearned = change.newValue as Boolean
                queries.updateIsLearned(isLearned, cardId)
            }
            Card::levelOfKnowledge -> {
                val levelOfKnowledge = change.newValue as Int
                queries.updateLevelOfKnowledge(levelOfKnowledge, cardId)
            }
            Card::lastAnsweredAt -> {
                val lastAnsweredAt = change.newValue as DateTime?
                val databaseValue = lastAnsweredAt?.unixMillisLong
                queries.updateLastAnsweredAt(databaseValue, cardId)
            }
        }
    }
}