package com.odnovolov.forgetmenot.persistence.longterm.globalstate.writingchanges

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.entity.SpeakEvent
import com.odnovolov.forgetmenot.domain.entity.SpeakPlan
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
                if (change !is PropertyValueChange) return
                val newSpeakEvents = change.newValue as List<SpeakEvent>
                database.speakPlanQueries.updateSpeakEvents(newSpeakEvents, speakPlanId)
            }
        }
    }
}