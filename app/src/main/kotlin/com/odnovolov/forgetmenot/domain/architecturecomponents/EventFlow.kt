package com.odnovolov.forgetmenot.domain.architecturecomponents

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class EventFlow<Event> {
    private val channels: MutableList<Channel<Event>> = ArrayList()

    fun get(): Flow<Event> = flow {
        val channel = Channel<Event>(capacity = UNLIMITED)
        channels.add(channel)
        try {
            for (event: Event in channel) {
                emit(event)
            }
        } finally {
            channels.remove(channel)
        }
    }

    fun send(event: Event) {
        channels.forEach { it.offer(event) }
    }
}