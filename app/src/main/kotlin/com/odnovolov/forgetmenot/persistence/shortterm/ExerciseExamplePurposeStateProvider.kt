package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.interactor.exercise.example.ExerciseExamplePurpose
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ExerciseExamplePurposeStateProvider(
    json: Json,
    database: Database,
    override val key: String
) : BaseSerializableStateProvider<ExerciseExamplePurpose, ExerciseExamplePurposeStateProvider.SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val purpose: ExerciseExamplePurpose
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: ExerciseExamplePurpose): SerializableState {
        return SerializableState(state)
    }

    override fun toOriginal(serializableState: SerializableState): ExerciseExamplePurpose {
        return serializableState.purpose
    }
}