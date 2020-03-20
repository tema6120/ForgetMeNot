package com.odnovolov.forgetmenot.persistence.serializablestate

import com.odnovolov.forgetmenot.persistence.serializablestate.DeckSettingsScreenStateProvider.SerializableDeckSettingsScreenState
import com.odnovolov.forgetmenot.presentation.common.entity.NamePresetDialogStatus
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsScreenState
import kotlinx.serialization.Serializable

class DeckSettingsScreenStateProvider :
    BaseSerializableStateProvider<DeckSettingsScreenState, SerializableDeckSettingsScreenState>() {
    @Serializable
    data class SerializableDeckSettingsScreenState(
        val isRenameDeckDialogVisible: Boolean,
        val typedDeckName: String,
        val namePresetDialogStatus: NamePresetDialogStatus,
        val typedPresetName: String,
        val renamePresetId: Long?
    )

    override val serializer = SerializableDeckSettingsScreenState.serializer()
    override val serializableClassName = SerializableDeckSettingsScreenState::class.java.name

    override fun toSerializable(state: DeckSettingsScreenState) =
        SerializableDeckSettingsScreenState(
            state.isRenameDeckDialogVisible,
            state.typedDeckName,
            state.namePresetDialogStatus,
            state.typedPresetName,
            state.renamePresetId
        )

    override fun toOriginal(serializableState: SerializableDeckSettingsScreenState) =
        DeckSettingsScreenState().apply {
            isRenameDeckDialogVisible = serializableState.isRenameDeckDialogVisible
            typedDeckName = serializableState.typedDeckName
            namePresetDialogStatus = serializableState.namePresetDialogStatus
            typedPresetName = serializableState.typedPresetName
            renamePresetId = serializableState.renamePresetId
        }
}