package com.odnovolov.forgetmenot.presentation.common.base

import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow

abstract class BaseController<Event, Command> {
    protected val coroutineScope = CoroutineScope(Job() + businessLogicThread)
    private val commandFlow by lazy { EventFlow<Command>() }
    val commands: Flow<Command> get() = commandFlow.get()
    protected open val autoSave = true

    init {
        coroutineScope.launch {
            delay(50)
            if (autoSave) saveState()
        }
    }

    fun dispatch(event: Event) {
        coroutineScope.launch {
            handle(event)
            if (autoSave) saveState()
        }
    }

    protected abstract fun handle(event: Event)

    protected abstract fun saveState()

    protected fun sendCommand(command: Command, postponeIfNotActive: Boolean = false) {
        commandFlow.send(command, postponeIfNotActive)
    }

    open fun dispose() {
        coroutineScope.cancel()
    }
}