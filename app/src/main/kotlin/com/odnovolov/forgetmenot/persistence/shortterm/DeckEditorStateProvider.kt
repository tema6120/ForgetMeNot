package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.DeckEditor
import com.odnovolov.forgetmenot.persistence.shortterm.DeckEditorStateProvider.SerializableState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class DeckEditorStateProvider(
    json: Json,
    database: Database,
    private val globalState: GlobalState,
    override val key: String = DeckEditor.State::class.qualifiedName!!
) : BaseSerializableStateProvider<DeckEditor.State, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val deckId: Long
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: DeckEditor.State) = SerializableState(state.deck.id)

    override fun toOriginal(serializableState: SerializableState): DeckEditor.State {
        val deck: Deck = globalState.decks.first { it.id == serializableState.deckId }
        return DeckEditor.State(deck)
    }
}