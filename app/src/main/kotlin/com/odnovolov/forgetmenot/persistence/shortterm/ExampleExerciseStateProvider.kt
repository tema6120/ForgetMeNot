package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ExampleExerciseStateProvider(
    json: Json,
    database: Database,
    override val key: String = "ExampleExerciseState"
) : BaseSerializableStateProvider<Boolean, ExampleExerciseStateProvider.SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val useTimer: Boolean
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: Boolean) = SerializableState(
        state
    )

    override fun toOriginal(serializableState: SerializableState): Boolean {
        return serializableState.useTimer
    }
}