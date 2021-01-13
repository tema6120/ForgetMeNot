package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.persistence.shortterm.DeckEditorScreenStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenState
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenState.DeckEditorScreenTab
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class DeckEditorScreenStateProvider(
    json: Json,
    database: Database,
    private val globalState: GlobalState,
    override val key: String = DeckEditorScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<DeckEditorScreenState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val deckId: Long,
        val initialTab: DeckEditorScreenTab
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: DeckEditorScreenState) = SerializableState(
        state.deck.id,
        state.initialTab
    )

    override fun toOriginal(serializableState: SerializableState): DeckEditorScreenState {
        val deck: Deck = globalState.decks.first { it.id == serializableState.deckId }
        return DeckEditorScreenState(deck, serializableState.initialTab)
    }
}