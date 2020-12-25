package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.shortterm.MotivationalTimerDialogStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.motivationaltimer.MotivationalTimerDialogState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class MotivationalTimerDialogStateProvider(
    json: Json,
    database: Database,
    override val key: String = MotivationalTimerDialogState::class.qualifiedName!!
) : BaseSerializableStateProvider<MotivationalTimerDialogState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val isTimerEnabled: Boolean,
        val timeInput: String
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: MotivationalTimerDialogState) = SerializableState(
        state.isTimerEnabled,
        state.timeInput
    )

    override fun toOriginal(serializableState: SerializableState) = MotivationalTimerDialogState(
        serializableState.isTimerEnabled,
        serializableState.timeInput
    )
}