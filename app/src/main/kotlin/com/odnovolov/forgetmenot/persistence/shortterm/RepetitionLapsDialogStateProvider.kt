package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.shortterm.RepetitionLapsDialogStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.laps.RepetitionLapsDialogState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class RepetitionLapsDialogStateProvider(
    json: Json,
    database: Database,
    override val key: String = RepetitionLapsDialogState::class.qualifiedName!!
) : BaseSerializableStateProvider<RepetitionLapsDialogState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val isInfinitely: Boolean,
        val numberOfLapsInput: String
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: RepetitionLapsDialogState) = SerializableState(
        state.isInfinitely,
        state.numberOfLapsInput
    )

    override fun toOriginal(serializableState: SerializableState) = RepetitionLapsDialogState(
        serializableState.isInfinitely,
        serializableState.numberOfLapsInput
    )
}