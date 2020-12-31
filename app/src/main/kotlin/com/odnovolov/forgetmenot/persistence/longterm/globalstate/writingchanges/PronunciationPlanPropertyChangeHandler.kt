package com.odnovolov.forgetmenot.persistence.longterm.globalstate.writingchanges

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.entity.PronunciationEvent
import com.odnovolov.forgetmenot.domain.entity.PronunciationPlan
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler

class PronunciationPlanPropertyChangeHandler(
    private val database: Database
) : PropertyChangeHandler {
    override fun handle(change: Change) {
        val pronunciationPlanId: Long = change.propertyOwnerId
        val exists: Boolean = database.pronunciationPlanQueries.exists(pronunciationPlanId)
            .executeAsOne()
        if (!exists) return
        when (change.property) {
            PronunciationPlan::pronunciationEvents -> {
                if (change !is PropertyValueChange) return
                val newPronunciationEvents = change.newValue as List<PronunciationEvent>
                database.pronunciationPlanQueries
                    .updatePronunciationEvents(newPronunciationEvents, pronunciationPlanId)
            }
        }
    }
}