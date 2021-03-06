package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ExampleExerciseStateUseTimerProvider(
    json: Json,
    database: Database,
    override val key: String
) : BaseSerializableStateProvider<Boolean, ExampleExerciseStateUseTimerProvider.SerializableState>(
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