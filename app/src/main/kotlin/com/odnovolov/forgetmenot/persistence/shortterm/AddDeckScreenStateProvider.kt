package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.shortterm.AddDeckScreenStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenState.HowToAdd
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
        val typedText: String,
        val howToAdd: HowToAdd?
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: AddDeckScreenState) = SerializableState(
        state.typedText,
        state.howToAdd
    )

    override fun toOriginal(serializableState: SerializableState) =
        AddDeckScreenState().apply {
            typedText = serializableState.typedText
            howToAdd = serializableState.howToAdd
        }
}