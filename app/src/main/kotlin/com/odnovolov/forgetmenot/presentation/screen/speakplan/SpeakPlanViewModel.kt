package com.odnovolov.forgetmenot.presentation.screen.speakplan

import SPEAK_PLAN_SCOPE_ID
import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.domain.entity.SpeakEvent
import com.odnovolov.forgetmenot.domain.entity.SpeakPlan
import kotlinx.coroutines.flow.Flow
import org.koin.java.KoinJavaComponent.getKoin

class SpeakPlanViewModel(
) : ViewModel() {

    val speakEvents: Flow<List<SpeakEvent>> = SpeakPlan.Default.flowOf(SpeakPlan::speakEvents)

    override fun onCleared() {
        getKoin().getScope(SPEAK_PLAN_SCOPE_ID).close()
    }
}