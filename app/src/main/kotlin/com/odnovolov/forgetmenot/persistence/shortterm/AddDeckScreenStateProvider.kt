package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.shortterm.AddDeckScreenStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class AddDeckScreenStateProvider(
    json: Json,
    database: Database,
    override val key: String = AddDeckScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<AddDeckScreenState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val typedText: String
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: AddDeckScreenState) = SerializableState(
        state.typedText
    )

    override fun toOriginal(serializableState: SerializableState) =
        AddDeckScreenState().apply {
            typedText = serializableState.typedText
        }
}