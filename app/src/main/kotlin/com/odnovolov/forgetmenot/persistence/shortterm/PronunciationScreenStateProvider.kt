package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.persistence.shortterm.PronunciationScreenStateProvider.SerializablePronunciationScreenState
import com.odnovolov.forgetmenot.presentation.common.entity.NamePresetDialogStatus
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationScreenState
import kotlinx.serialization.Serializable

class PronunciationScreenStateProvider :
    BaseSerializableStateProvider<PronunciationScreenState, SerializablePronunciationScreenState>() {
    @Serializable
    data class SerializablePronunciationScreenState(
        val namePresetDialogStatus: NamePresetDialogStatus,
        val typedPresetName: String,
        val renamePresetId: Long?
    )

    override val serializer = SerializablePronunciationScreenState.serializer()
    override val serializableClassName = SerializablePronunciationScreenState::class.java.name

    override fun toSerializable(
        state: PronunciationScreenState
    ) = SerializablePronunciationScreenState(
        state.namePresetDialogStatus,
        state.typedPresetName,
        state.renamePresetId
    )

    override fun toOriginal(serializableState: SerializablePronunciationScreenState) =
        PronunciationScreenState().apply {
            namePresetDialogStatus = serializableState.namePresetDialogStatus
            typedPresetName = serializableState.typedPresetName
            renamePresetId = serializableState.renamePresetId
        }
}