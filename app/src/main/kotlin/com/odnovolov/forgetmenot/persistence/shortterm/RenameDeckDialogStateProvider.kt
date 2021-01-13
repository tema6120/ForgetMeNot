package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.shortterm.RenameDeckDialogStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.renamedeck.RenameDeckDialogState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class RenameDeckDialogStateProvider(
    json: Json,
    database: Database,
    override val key: String = RenameDeckDialogState::class.qualifiedName!!
) : BaseSerializableStateProvider<RenameDeckDialogState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val typedDeckName: String
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: RenameDeckDialogState) = SerializableState(
        state.typedDeckName
    )

    override fun toOriginal(serializableState: SerializableState) = RenameDeckDialogState(
        serializableState.typedDeckName
    )
}