package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.SerializableDb
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

abstract class BaseSerializableStateProvider<State, SerializableState>(
    private val json: Json,
    database: Database
) : ShortTermStateProvider<State> {
    private val queries = database.serializableQueries
    protected abstract val serializer: KSerializer<SerializableState>
    abstract val key: String

    @Volatile
    private var savedSerializable: SerializableState? = null

    final override fun load(): State {
        val jsonData: String = queries.selectJsonData(key).executeAsOneOrNull()
            ?: throw NoSuchElementException("jsonData by key '$key' was not found")
        val serializableState: SerializableState = Json.decodeFromString(serializer, jsonData)
        return toOriginal(serializableState)
    }

    protected abstract fun toOriginal(serializableState: SerializableState): State

    final override fun save(state: State) {
        val serializable: SerializableState = toSerializable(state)
        if (serializable != savedSerializable) {
            GlobalScope.launch(Dispatchers.IO) {
                savedSerializable = serializable
                val jsonData: String = json.encodeToString(serializer, savedSerializable!!)
                val serializableDb = SerializableDb(key, jsonData)
                queries.replace(serializableDb)
            }
        }
    }

    protected abstract fun toSerializable(state: State): SerializableState
}