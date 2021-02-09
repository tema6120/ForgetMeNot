package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.shortterm.AddDeckScreenStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.home.addcards.AddCardsScreenState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class AddDeckScreenStateProvider(
    json: Json,
    database: Database,
    override val key: String = AddCardsScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<AddCardsScreenState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val typedText: String,
        val isDeckBeingCreated: Boolean
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: AddCardsScreenState) = SerializableState(
        state.typedText,
        state.isDeckBeingCreated
    )

    override fun toOriginal(serializableState: SerializableState) =
        AddCardsScreenState().apply {
            typedText = serializableState.typedText
            isDeckBeingCreated = serializableState.isDeckBeingCreated
        }
}