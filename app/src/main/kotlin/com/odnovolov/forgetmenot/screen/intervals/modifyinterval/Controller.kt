package com.odnovolov.forgetmenot.screen.intervals.modifyinterval

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.screen.intervals.modifyinterval.ModifyIntervalEvent.*

class ModifyIntervalController : BaseController<ModifyIntervalEvent, Nothing>() {
    private val queries: ModifyIntervalControllerQueries = database.modifyIntervalControllerQueries

    override fun handleEvent(event: ModifyIntervalEvent) {
        when (event) {
            is IntervalNumberChanged -> {
                val intervalNumber: Long? = event.intervalNumberText.toString().toLongOrNull()
                queries.setIntervalNumber(intervalNumber)
            }

            is IntervalUnitChanged -> {
                queries.setIntervalUnit(event.intervalUnit.name)
            }

            OkButtonClicked -> {
                queries.updateInterval()
            }
        }
    }
}