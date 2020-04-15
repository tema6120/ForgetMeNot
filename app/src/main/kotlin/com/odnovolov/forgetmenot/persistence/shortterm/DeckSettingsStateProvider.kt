package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.persistence.shortterm.DeckSettingsStateProvider.SerializableDeckSettingsState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class DeckSettingsStateProvider(
    json: Json,
    database: Database,
    private val globalState: GlobalState,
    override val key: String = DeckSettings.State::class.qualifiedName!!
) : BaseSerializableStateProvider<DeckSettings.State, SerializableDeckSettingsState>(
    json,
    database
) {
    @Serializable
    data class SerializableDeckSettingsState(
        val deckId: Long
    )

    override val serializer = SerializableDeckSettingsState.serializer()

    override fun toSerializable(state: DeckSettings.State) =
        SerializableDeckSettingsState(state.deck.id)

    override fun toOriginal(serializableState: SerializableDeckSettingsState): DeckSettings.State {
        val deck: Deck = globalState.decks.first { it.id == serializableState.deckId }
        return DeckSettings.State(deck)
    }
}