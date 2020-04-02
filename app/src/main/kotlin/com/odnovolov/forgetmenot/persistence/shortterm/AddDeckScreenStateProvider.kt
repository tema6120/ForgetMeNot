package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.persistence.shortterm.AddDeckScreenStateProvider.SerializableAddDeckScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenState
import kotlinx.serialization.Serializable

class AddDeckScreenStateProvider
    : BaseSerializableStateProvider<AddDeckScreenState, SerializableAddDeckScreenState>() {
    @Serializable
    data class SerializableAddDeckScreenState(
        val typedText: String
    )

    override val serializer = SerializableAddDeckScreenState.serializer()
    override val serializableClassName: String = SerializableAddDeckScreenState::class.java.name
    override val defaultState = AddDeckScreenState()

    override fun toSerializable(state: AddDeckScreenState) = SerializableAddDeckScreenState(
        state.typedText
    )

    override fun toOriginal(serializableState: SerializableAddDeckScreenState) =
        AddDeckScreenState().apply {
            typedText = serializableState.typedText
        }
}