package com.odnovolov.forgetmenot.persistence.longterm.globalstate.writingchanges

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler
import com.soywiz.klock.DateTime

class CardPropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.cardQueries

    override fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        val cardId: Long = change.propertyOwnerId
        val exists: Boolean = queries.exists(cardId).executeAsOne()
        if (!exists) return
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