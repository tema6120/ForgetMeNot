package com.odnovolov.forgetmenot.screen.intervals

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.intervals.IntervalsControllerQueries
import com.odnovolov.forgetmenot.intervals.modifyinterval.TempModifyIntervalState
import com.odnovolov.forgetmenot.screen.intervals.IntervalsEvent.*
import com.odnovolov.forgetmenot.screen.intervals.IntervalsOrder.ShowModifyIntervalDialog

class IntervalsController : BaseController<IntervalsEvent, IntervalsOrder>() {
    private val queries: IntervalsControllerQueries = database.intervalsControllerQueries

    override fun handleEvent(event: IntervalsEvent) {
        return when (event) {
            is ModifyIntervalButtonClicked -> {
                val interval = queries
                    .getIntervalByTargetLevelOfKnowledge(event.targetLevelOfKnowledge)
                    .executeAsOne()
                val chunks = interval.value.split(" ")
                val intervalNumber: Long = chunks[0].toLong()
                val intervalUnit: String = chunks[1]
                val modifyIntervalState = TempModifyIntervalState.Impl(
                    interval.id,
                    intervalNumber,
                    intervalUnit
                )
                with(database.modifyIntervalInitQueries) {
                    createStateIfNotExists()
                    cleanState()
                    initState(modifyIntervalState)
                }
                issueOrder(ShowModifyIntervalDialog)
            }

            AddIntervalButtonClicked -> {

            }

            RemoveIntervalButtonClicked -> {

            }
        }
    }
}