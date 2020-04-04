package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.persistence.shortterm.PresetDialogStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.common.preset.DialogPurpose
import com.odnovolov.forgetmenot.presentation.common.preset.PresetDialogState
import kotlinx.serialization.Serializable

class PresetDialogStateProvider(
    override val key: String
): BaseSerializableStateProvider<PresetDialogState, SerializableState>() {
    @Serializable
    data class SerializableState(
        val purpose: DialogPurpose?,
        val typedPresetName: String
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: PresetDialogState) = SerializableState(
        state.purpose,
        state.typedPresetName
    )

    override fun toOriginal(serializableState: SerializableState) = PresetDialogState().apply {
        purpose = serializableState.purpose
        typedPresetName = serializableState.typedPresetName
    }
}