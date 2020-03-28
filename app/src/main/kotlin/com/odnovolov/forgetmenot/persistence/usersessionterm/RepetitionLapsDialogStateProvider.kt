package com.odnovolov.forgetmenot.persistence.usersessionterm

import com.odnovolov.forgetmenot.persistence.usersessionterm.RepetitionLapsDialogStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.laps.RepetitionLapsDialogState
import kotlinx.serialization.Serializable

class RepetitionLapsDialogStateProvider
    : BaseSerializableStateProvider<RepetitionLapsDialogState, SerializableState>() {
    @Serializable
    data class SerializableState(
        val isInfinitely: Boolean,
        val numberOfLapsInput: String
    )

    override val serializer = SerializableState.serializer()
    override val serializableClassName: String = SerializableState::class.java.name

    override fun toSerializable(state: RepetitionLapsDialogState) = SerializableState(
        state.isInfinitely,
        state.numberOfLapsInput
    )

    override fun toOriginal(serializableState: SerializableState) = RepetitionLapsDialogState(
        serializableState.isInfinitely,
        serializableState.numberOfLapsInput
    )
}