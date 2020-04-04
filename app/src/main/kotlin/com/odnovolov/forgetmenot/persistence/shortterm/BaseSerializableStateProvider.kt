package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.persistence.SerializableDb
import com.odnovolov.forgetmenot.persistence.database
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

private val json = Json(JsonConfiguration.Stable)

abstract class BaseSerializableStateProvider<State, SerializableState>
    : ShortTermStateProvider<State> {
    private val queries get() = database.serializableQueries
    protected abstract val serializer: KSerializer<SerializableState>
    abstract val key: String

    final override fun load(): State {
        val jsonData: String = queries.selectJsonData(key).executeAsOneOrNull()
            ?: throw NoSuchElementException("jsonData by key '$key' was not found")
        val serializableState: SerializableState = json.parse(serializer, jsonData)
        return toOriginal(serializableState)
    }

    protected abstract fun toOriginal(serializableState: SerializableState): State

    final override fun save(state: State) {
        val serializable: SerializableState = toSerializable(state)
        val jsonData: String = json.stringify(serializer, serializable)
        val serializableDb = SerializableDb.Impl(key, jsonData)
        queries.replace(serializableDb)
    }

    protected abstract fun toSerializable(state: State): SerializableState
}