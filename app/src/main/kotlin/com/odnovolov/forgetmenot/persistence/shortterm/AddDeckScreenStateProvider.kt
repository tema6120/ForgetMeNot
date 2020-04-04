package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.persistence.shortterm.AddDeckScreenStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenState
import kotlinx.serialization.Serializable

class AddDeckScreenStateProvider(
    override val key: String = AddDeckScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<AddDeckScreenState, SerializableState>() {
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