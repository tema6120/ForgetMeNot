package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.persistence.shortterm.DeckSetupScreenStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.decksetup.DeckSetupScreenState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class DeckSetupScreenStateProvider(
    json: Json,
    database: Database,
    private val globalState: GlobalState,
    override val key: String = DeckSetupScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<DeckSetupScreenState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val deckId: Long,
        val typedDeckName: String
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: DeckSetupScreenState) = SerializableState(
        state.relevantDeck.id,
        state.typedDeckName
    )

    override fun toOriginal(serializableState: SerializableState): DeckSetupScreenState {
        val deck: Deck = globalState.decks.first { it.id == serializableState.deckId }
        return DeckSetupScreenState(deck, serializableState.typedDeckName)
    }
}