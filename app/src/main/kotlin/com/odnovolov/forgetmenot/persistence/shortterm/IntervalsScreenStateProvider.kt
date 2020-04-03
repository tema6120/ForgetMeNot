package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.persistence.shortterm.IntervalsScreenStateProvider.SerializableIntervalsScreenState
import com.odnovolov.forgetmenot.presentation.common.entity.NamePresetDialogStatus
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsScreenState
import kotlinx.serialization.Serializable

class IntervalsScreenStateProvider
    : BaseSerializableStateProvider<IntervalsScreenState, SerializableIntervalsScreenState>() {
    @Serializable
    data class SerializableIntervalsScreenState(
        val namePresetDialogStatus: NamePresetDialogStatus,
        val typedPresetName: String,
        val renamePresetId: Long?
    )

    override val serializer = SerializableIntervalsScreenState.serializer()
    override val serializableId = SerializableIntervalsScreenState::class.java.name

    override fun toSerializable(state: IntervalsScreenState) = SerializableIntervalsScreenState(
        state.namePresetDialogStatus,
        state.typedPresetName,
        state.renamePresetId
    )

    override fun toOriginal(serializableState: SerializableIntervalsScreenState) =
        IntervalsScreenState().apply {
            namePresetDialogStatus = serializableState.namePresetDialogStatus
            typedPresetName = serializableState.typedPresetName
            renamePresetId = serializableState.renamePresetId
        }
}