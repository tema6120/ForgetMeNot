package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.shortterm.DeckChooserScreenStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserScreenState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class DeckChooserScreenStateProvider(
    json: Json,
    database: Database,
    override val key: String = DeckChooserScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<DeckChooserScreenState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val purpose: DeckChooserScreenState.Purpose
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: DeckChooserScreenState) = SerializableState(
        state.purpose
    )

    override fun toOriginal(serializableState: SerializableState): DeckChooserScreenState =
        DeckChooserScreenState(serializableState.purpose)
}