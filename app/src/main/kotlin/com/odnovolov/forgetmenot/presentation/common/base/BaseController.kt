package com.odnovolov.forgetmenot.presentation.common.base

import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow

abstract class BaseController<Event, Command> {
    protected val coroutineScope = CoroutineScope(Job() + controllerDispatcher)
    private val commandFlow = EventFlow<Command>()
    val commands: Flow<Command> = commandFlow.get()

    fun dispatch(event: Event) {
        coroutineScope.launch {
            handle(event)
            saveState()
        }
    }

    protected abstract fun handle(event: Event)

    protected abstract fun saveState()

    protected fun sendCommand(command: Command) {
        commandFlow.send(command)
    }

    fun dispose() {
        coroutineScope.cancel()
    }
}

private val controllerDispatcher = newSingleThreadContext("Controller Thread")