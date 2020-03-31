package com.odnovolov.forgetmenot.presentation.screen.speakplan

import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import kotlinx.coroutines.flow.Flow

class SpeakPlanController(
    private val longTermStateSaver: LongTermStateSaver
) {
    sealed class Command {
        class MyCommand(val text: String) : Command()
    }

    private val commandFlow = EventFlow<Command>()
    val commands: Flow<Command> = commandFlow.get()

    fun onFragmentPause() {
    }
}