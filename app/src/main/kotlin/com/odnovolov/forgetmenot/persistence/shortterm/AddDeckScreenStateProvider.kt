package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.persistence.shortterm.AddDeckScreenStateProvider.SerializableAddDeckScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenState
import kotlinx.serialization.Serializable

class AddDeckScreenStateProvider(
    override val serializableId: String = AddDeckScreenState::class.simpleName!!,
    override val defaultState: AddDeckScreenState? = null
) : BaseSerializableStateProvider<AddDeckScreenState, SerializableAddDeckScreenState>() {
    @Serializable
    data class SerializableAddDeckScreenState(
        val typedText: String
    )

    override val serializer = SerializableAddDeckScreenState.serializer()

    override fun toSerializable(state: AddDeckScreenState) = SerializableAddDeckScreenState(
        state.typedText
    )

    override fun toOriginal(serializableState: SerializableAddDeckScreenState) =
        AddDeckScreenState().apply {
            typedText = serializableState.typedText
        }
}