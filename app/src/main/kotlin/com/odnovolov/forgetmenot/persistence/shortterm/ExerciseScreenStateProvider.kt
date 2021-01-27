package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreenState
import kotlinx.serialization.json.Json

class ExerciseScreenStateProvider(
    json: Json,
    database: Database,
    override val key: String = ExerciseScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<ExerciseScreenState, ExerciseScreenState>(
    json,
    database
) {
    override val serializer = ExerciseScreenState.serializer()

    override fun toSerializable(state: ExerciseScreenState) = state

    override fun toOriginal(serializableState: ExerciseScreenState): ExerciseScreenState = serializableState
}