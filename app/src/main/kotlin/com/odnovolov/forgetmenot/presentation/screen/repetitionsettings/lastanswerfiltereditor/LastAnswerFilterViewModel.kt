package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.lastanswerfiltereditor

import LAST_ANSWER_FILTER_SCOPE_ID
import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.java.KoinJavaComponent.getKoin

class LastAnswerFilterViewModel(
    private val dialogState: LastAnswerFilterDialogState
) : ViewModel() {
    val isFromDialog: Boolean = dialogState.isFromDialog

    val intervalValueText: String
        get() = dialogState.timeAgo.value?.toString() ?: ""

    val displayedIntervalUnit: DisplayedInterval.IntervalUnit
        get() = dialogState.timeAgo.intervalUnit

    val isZeroTimeSelected: Flow<Boolean> =
        dialogState.flowOf(LastAnswerFilterDialogState::isZeroTimeSelected)

    val isOkButtonEnabled: Flow<Boolean> = combine(
        isZeroTimeSelected,
        dialogState.timeAgo.asFlow()
    ) { isZeroTimeSelected: Boolean, timeAgo: DisplayedInterval ->
        isZeroTimeSelected || timeAgo.isValid()
    }

    override fun onCleared() {
        getKoin().getScope(LAST_ANSWER_FILTER_SCOPE_ID).close()
    }
}