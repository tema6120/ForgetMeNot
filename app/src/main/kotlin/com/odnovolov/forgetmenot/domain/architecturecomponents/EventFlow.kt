package com.odnovolov.forgetmenot.domain.architecturecomponents

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.CopyOnWriteArrayList

class EventFlow<Event> {
    private val channels: MutableList<Channel<Event>> = CopyOnWriteArrayList()
    private val pendingEvents: MutableList<Event> = CopyOnWriteArrayList()

    fun get(): Flow<Event> = flow {
        val channel = Channel<Event>(capacity = UNLIMITED)
        pendingEvents.forEach(channel::offer)
        pendingEvents.clear()
        channels.add(channel)
        try {
            for (event: Event in channel) {
                emit(event)
            }
        } finally {
            channels.remove(channel)
        }
    }

    fun send(event: Event, postponeIfNotActive: Boolean = false) {
        if (channels.isEmpty()) {
            if (postponeIfNotActive) pendingEvents.add(event)
        } else {
            channels.forEach { it.offer(event) }
        }
    }
}