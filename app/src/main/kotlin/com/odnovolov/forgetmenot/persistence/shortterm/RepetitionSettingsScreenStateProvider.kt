package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.persistence.shortterm.RepetitionSettingsScreenStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.common.entity.NamePresetDialogStatus
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.RepetitionSettingsScreenState
import kotlinx.serialization.Serializable

class RepetitionSettingsScreenStateProvider
    : BaseSerializableStateProvider<RepetitionSettingsScreenState, SerializableState>() {
    @Serializable
    data class SerializableState(
        val namePresetDialogStatus: NamePresetDialogStatus,
        val typedPresetName: String,
        val renamePresetId: Long?
    )

    override val serializer = SerializableState.serializer()
    override val serializableId: String = SerializableState::class.simpleName!!

    override fun toSerializable(state: RepetitionSettingsScreenState) = SerializableState(
        state.namePresetDialogStatus,
        state.typedPresetName,
        state.renamePresetId
    )

    override fun toOriginal(serializableState: SerializableState) =
        RepetitionSettingsScreenState().apply {
        namePresetDialogStatus = serializableState.namePresetDialogStatus
        typedPresetName = serializableState.typedPresetName
        renamePresetId = serializableState.renamePresetId
    }
}