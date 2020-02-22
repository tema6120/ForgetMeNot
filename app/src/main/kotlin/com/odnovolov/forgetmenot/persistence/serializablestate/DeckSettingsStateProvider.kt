package com.odnovolov.forgetmenot.persistence.serializablestate

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import kotlinx.serialization.Serializable

object DeckSettingsStateProvider {
    fun load(globalState: GlobalState): DeckSettings.State {
        return loadSerializable(SerializableDeckSettingsState.serializer())
            ?.toOriginal(globalState)
            ?: throw IllegalStateException("No DeckSettings.State in the Store")
    }

    fun save(deckSettingsState: DeckSettings.State) {
        val serializable: SerializableDeckSettingsState = deckSettingsState.toSerializable()
        saveSerializable(serializable, SerializableDeckSettingsState.serializer())
    }

    fun delete() {
        deleteSerializable(SerializableDeckSettingsState::class)
    }

    @Serializable
    private data class SerializableDeckSettingsState(
        val deckId: Long
    )

    private fun DeckSettings.State.toSerializable() = SerializableDeckSettingsState(this.deck.id)

    private fun SerializableDeckSettingsState.toOriginal(
        globalState: GlobalState
    ): DeckSettings.State {
        val deck: Deck = globalState.decks.find { it.id == this.deckId }!!
        return DeckSettings.State(deck)
    }
}