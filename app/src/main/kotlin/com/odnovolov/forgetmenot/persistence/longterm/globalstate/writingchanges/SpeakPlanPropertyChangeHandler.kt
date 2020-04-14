package com.odnovolov.forgetmenot.persistence.longterm.globalstate.writingchanges

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.ListChange
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.entity.SpeakEvent
import com.odnovolov.forgetmenot.domain.entity.SpeakEvent.*
import com.odnovolov.forgetmenot.domain.entity.SpeakPlan
import com.odnovolov.forgetmenot.persistence.globalstate.DelaySpeakEventDb
import com.odnovolov.forgetmenot.persistence.globalstate.SpeakEventDb
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler

class SpeakPlanPropertyChangeHandler(
    private val database: Database
) : PropertyChangeHandler {
    override fun handle(change: Change) {
        val speakPlanId: Long = change.propertyOwnerId
        val exists: Boolean = database.speakPlanQueries.exists(speakPlanId).executeAsOne()
        if (!exists) return
        when (change.property) {
            SpeakPlan::name -> {
                if (change !is PropertyValueChange) return
                val newName = change.newValue as String
                database.speakPlanQueries.updateName(newName, speakPlanId)
            }
            SpeakPlan::speakEvents -> {
                if (change !is ListChange) return
                change.removedItemsAt.forEach { ordinal: Int ->
                    database.speakEventQueries.delete(speakPlanId, ordinal)
                }
                change.movedItemsAt.forEach { (oldOrdinal: Int, newOrdinal: Int) ->
                    database.speakEventQueries.updateOrdinal(newOrdinal, speakPlanId, oldOrdinal)
                }
                (change.addedItems as Map<Int, SpeakEvent>).forEach { (ordinal, speakEvent) ->
                    insertSpeakEvent(speakEvent, speakPlanId, ordinal)
                }
            }
        }
    }

    fun insertSpeakEvent(speakEvent: SpeakEvent, speakPlanId: Long, ordinal: Int) {
        when (speakEvent) {
            is SpeakQuestion -> {
                val speakEventDb = SpeakEventDb.Impl(
                    speakEvent.id,
                    speakPlanId,
                    ordinal,
                    SpeakQuestion::class.simpleName!!
                )
                database.speakEventQueries.insert(speakEventDb)
            }
            is SpeakAnswer -> {
                val speakEventDb = SpeakEventDb.Impl(
                    speakEvent.id,
                    speakPlanId,
                    ordinal,
                    SpeakAnswer::class.simpleName!!
                )
                database.speakEventQueries.insert(speakEventDb)
            }
            is Delay -> {
                val speakEventDb = SpeakEventDb.Impl(
                    speakEvent.id,
                    speakPlanId,
                    ordinal,
                    Delay::class.simpleName!!
                )
                database.speakEventQueries.insert(speakEventDb)
                val delaySpeakEventDb = DelaySpeakEventDb.Impl(
                    speakEventId = speakEvent.id,
                    delayMs = speakEvent.timeSpan.millisecondsLong
                )
                database.delaySpeakEventQueries.insert(delaySpeakEventDb)
            }
        }
    }
}