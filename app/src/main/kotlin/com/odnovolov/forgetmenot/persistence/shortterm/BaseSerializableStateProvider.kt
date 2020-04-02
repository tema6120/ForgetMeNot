package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.persistence.database
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

private val json = Json(JsonConfiguration.Stable)

abstract class BaseSerializableStateProvider<State, SerializableState>
    : ShortTermStateProvider<State> {
    private val queries = database.serializableQueries
    abstract val serializer: KSerializer<SerializableState>
    abstract val serializableClassName: String
    open val defaultState: State? = null

    abstract fun toSerializable(state: State): SerializableState

    abstract fun toOriginal(serializableState: SerializableState): State

    override fun load(): State {
        val jsonData: String = queries.selectJsonData(serializableClassName).executeAsOneOrNull()
            ?: defaultState?.let { return it }
            ?: throw IllegalStateException("No $serializableClassName in db")
        val serializableState: SerializableState = json.parse(serializer, jsonData)
        return toOriginal(serializableState)
    }

    override fun save(state: State) {
        val serializable: SerializableState = toSerializable(state)
        val jsonData: String = json.stringify(serializer, serializable)
        queries.replace(serializableClassName, jsonData)
    }
}