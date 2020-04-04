package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.persistence.shortterm.DeckSettingsScreenStateProvider.SerializableDeckSettingsScreenState
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsScreenState
import kotlinx.serialization.Serializable

class DeckSettingsScreenStateProvider :
    BaseSerializableStateProvider<DeckSettingsScreenState, SerializableDeckSettingsScreenState>() {
    @Serializable
    data class SerializableDeckSettingsScreenState(
        val isRenameDeckDialogVisible: Boolean,
        val typedDeckName: String
    )

    override val serializer = SerializableDeckSettingsScreenState.serializer()
    override val serializableId = SerializableDeckSettingsScreenState::class.simpleName!!

    override fun toSerializable(state: DeckSettingsScreenState) =
        SerializableDeckSettingsScreenState(
            state.isRenameDeckDialogVisible,
            state.typedDeckName
        )

    override fun toOriginal(serializableState: SerializableDeckSettingsScreenState) =
        DeckSettingsScreenState().apply {
            isRenameDeckDialogVisible = serializableState.isRenameDeckDialogVisible
            typedDeckName = serializableState.typedDeckName
        }
}