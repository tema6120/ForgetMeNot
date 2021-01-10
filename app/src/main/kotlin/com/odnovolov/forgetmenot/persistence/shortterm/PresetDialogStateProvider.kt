package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.shortterm.PresetDialogStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.preset.DialogPurpose
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.preset.PresetDialogState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class PresetDialogStateProvider(
    json: Json,
    database: Database,
    override val key: String
): BaseSerializableStateProvider<PresetDialogState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val purpose: DialogPurpose?,
        val typedPresetName: String,
        val idToDelete: Long?
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: PresetDialogState) = SerializableState(
        state.purpose,
        state.typedPresetName,
        state.idToDelete
    )

    override fun toOriginal(serializableState: SerializableState) = PresetDialogState().apply {
        purpose = serializableState.purpose
        typedPresetName = serializableState.typedPresetName
        idToDelete = serializableState.idToDelete
    }
}