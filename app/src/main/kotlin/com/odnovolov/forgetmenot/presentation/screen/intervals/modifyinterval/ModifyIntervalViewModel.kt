package com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval

import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.KoinComponent

class ModifyIntervalViewModel(
    private val modifyIntervalDialogState: ModifyIntervalDialogState
) : ViewModel(), KoinComponent {

    val intervalValueText: String
        get() = modifyIntervalDialogState.displayedInterval.value?.toString() ?: ""

    val displayedIntervalUnit: DisplayedInterval.IntervalUnit
        get() = modifyIntervalDialogState.displayedInterval.intervalUnit

    val isOkButtonEnabled: Flow<Boolean> = modifyIntervalDialogState.displayedInterval.asFlow()
        .map { it.isValid() }

    override fun onCleared() {
        getKoin().getScope(MODIFY_INTERVAL_SCOPE_ID).close()
    }
}