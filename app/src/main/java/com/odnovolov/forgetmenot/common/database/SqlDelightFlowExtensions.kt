package com.odnovolov.forgetmenot.common.database

import com.squareup.sqldelight.Query
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.*

fun <T : Any> Query<T>.asFlow(): Flow<Query<T>> = flow {
    emit(this@asFlow)

    val channel = Channel<Unit>(CONFLATED)
    val listener = object : Query.Listener {
        override fun queryResultsChanged() {
            channel.offer(Unit)
        }
    }
    addListener(listener)
    try {
        for (item in channel) {
            emit(this@asFlow)
        }
    } finally {
        removeListener(listener)
    }
}

fun <T : Any> Flow<Query<T>>.mapToOne(): Flow<T> {
    return map { it.executeAsOne() }
        .flowOn(IO)
}

fun <T : Any> Flow<Query<T>>.mapToOneOrDefault(defaultValue: T): Flow<T> {
    return map { it.executeAsOneOrNull() ?: defaultValue }
        .flowOn(IO)
}

fun <T : Any> Flow<Query<T>>.mapToOneOrNull(): Flow<T?> {
    return map { it.executeAsOneOrNull() }
        .flowOn(IO)
}

fun <T : Any> Flow<Query<T>>.mapToOneNotNull(): Flow<T> {
    return mapNotNull { it.executeAsOneOrNull() }
        .flowOn(IO)
}

fun <T : Any> Flow<Query<T>>.mapToList(): Flow<List<T>> {
    return map { it.executeAsList() }
        .flowOn(IO)
}