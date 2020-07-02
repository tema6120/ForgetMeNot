package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.shortterm.DeckSetupScreenStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.decksetup.DeckSetupScreenState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class DeckSetupScreenStateProvider(
    json: Json,
    database: Database,
    override val key: String = DeckSetupScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<DeckSetupScreenState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(val typedDeckName: String)

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: DeckSetupScreenState) =
        SerializableState(state.typedDeckName)

    override fun toOriginal(serializableState: SerializableState) =
        DeckSetupScreenState().apply { typedDeckName = serializableState.typedDeckName }
}