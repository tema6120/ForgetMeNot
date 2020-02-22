package com.odnovolov.forgetmenot.persistence.serializablestate

import com.odnovolov.forgetmenot.common.entity.NamePresetDialogStatus
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsScreenState
import kotlinx.serialization.Serializable

object DeckSettingsScreenStateProvider {
    fun load(): DeckSettingsScreenState {
        return loadSerializable(SerializableDeckSettingsScreenState.serializer())
            ?.toOriginal()
            ?: throw  IllegalStateException("No DeckSettingsScreenState in the Store")
    }

    fun save(deckSettingsScreenState: DeckSettingsScreenState) {
        val serializable: SerializableDeckSettingsScreenState =
            deckSettingsScreenState.toSerializable()
        saveSerializable(serializable, SerializableDeckSettingsScreenState.serializer())
    }

    fun delete() {
        deleteSerializable(SerializableDeckSettingsScreenState::class)
    }

    @Serializable
    private data class SerializableDeckSettingsScreenState(
        val isRenameDeckDialogVisible: Boolean,
        val typedDeckName: String,
        val namePresetDialogStatus: NamePresetDialogStatus,
        val typedPresetName: String,
        val renamePresetId: Long?
    )

    private fun DeckSettingsScreenState.toSerializable() = SerializableDeckSettingsScreenState(
        isRenameDeckDialogVisible,
        typedDeckName,
        namePresetDialogStatus,
        typedPresetName,
        renamePresetId
    )

    private fun SerializableDeckSettingsScreenState.toOriginal() = DeckSettingsScreenState().also {
        it.isRenameDeckDialogVisible = this.isRenameDeckDialogVisible
        it.typedDeckName = this.typedDeckName
        it.namePresetDialogStatus = this.namePresetDialogStatus
        it.typedPresetName = this.typedPresetName
        it.renamePresetId = this.renamePresetId
    }
}