package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.persistence.shortterm.DeckSettingsScreenStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsScreenState
import kotlinx.serialization.Serializable

class DeckSettingsScreenStateProvider(
    override val key: String = DeckSettingsScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<DeckSettingsScreenState, SerializableState>() {
    @Serializable
    data class SerializableState(
        val isRenameDeckDialogVisible: Boolean,
        val typedDeckName: String
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: DeckSettingsScreenState) =
        SerializableState(
            state.isRenameDeckDialogVisible,
            state.typedDeckName
        )

    override fun toOriginal(serializableState: SerializableState) =
        DeckSettingsScreenState().apply {
            isRenameDeckDialogVisible = serializableState.isRenameDeckDialogVisible
            typedDeckName = serializableState.typedDeckName
        }
}