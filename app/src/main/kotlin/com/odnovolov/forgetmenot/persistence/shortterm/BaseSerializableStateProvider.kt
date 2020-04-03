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
    private val queries = database.serializableQueries
    protected abstract val serializer: KSerializer<SerializableState>
    abstract val serializableId: String
    open val defaultState: State? = null

    final override fun load(): State {
        val jsonData: String = queries.selectJson(serializableId).executeAsOneOrNull()
            ?: defaultState?.let { return it }
            ?: throw IllegalStateException("No json in db by id '$serializableId'")
        val serializableState: SerializableState = json.parse(serializer, jsonData)
        return toOriginal(serializableState)
    }

    protected abstract fun toOriginal(serializableState: SerializableState): State

    final override fun save(state: State) {
        val serializable: SerializableState = toSerializable(state)
        val jsonData: String = json.stringify(serializer, serializable)
        val serializableDb = SerializableDb.Impl(serializableId, jsonData)
        queries.replace(serializableDb)
    }

    protected abstract fun toSerializable(state: State): SerializableState
}