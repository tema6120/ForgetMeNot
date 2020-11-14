package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.persistence.shortterm.HomeScreenStateProvider.SerializableHomeScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSelection
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class HomeScreenStateProvider(
    json: Json,
    database: Database,
    private val globalState: GlobalState,
    override val key: String = HomeScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<HomeScreenState, SerializableHomeScreenState>(
    json,
    database
) {
    @Serializable
    data class SerializableHomeScreenState(
        val searchText: String,
        val deckSelection: DeckSelection?,
        val exportedDeckId: Long?
    )

    override val serializer = SerializableHomeScreenState.serializer()

    override fun toSerializable(state: HomeScreenState) = SerializableHomeScreenState(
        state.searchText,
        state.deckSelection,
        state.exportedDeck?.id
    )

    override fun toOriginal(serializableState: SerializableHomeScreenState): HomeScreenState {
        val exportDeck: Deck? = serializableState.exportedDeckId?.let { exportedDeckId: Long ->
            globalState.decks.first { deck -> deck.id == exportedDeckId }
        }
        return HomeScreenState().apply {
            searchText = serializableState.searchText
            deckSelection = serializableState.deckSelection
            exportedDeck = exportDeck
        }
    }
}