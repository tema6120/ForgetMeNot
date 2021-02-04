package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.AbstractDeck
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.ExistingDeck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.persistence.shortterm.RenameDeckDialogStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDialogState
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportDiScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class RenameDeckDialogStateProvider(
    json: Json,
    database: Database,
    private val globalState: GlobalState,
    override val key: String = RenameDeckDialogState::class.qualifiedName!!
) : BaseSerializableStateProvider<RenameDeckDialogState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val deckId: Long?,
        val typedDeckName: String
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: RenameDeckDialogState): SerializableState {
        val abstractDeck = state.abstractDeck
        val deckId = if (abstractDeck is ExistingDeck) abstractDeck.deck.id else null
        return SerializableState(deckId, state.typedDeckName)
    }

    override fun toOriginal(serializableState: SerializableState): RenameDeckDialogState {
        val abstractDeck: AbstractDeck = if (serializableState.deckId != null) {
            val deck: Deck = globalState.decks.first { it.id == serializableState.deckId }
            ExistingDeck(deck)
        } else {
            with(FileImportDiScope.getOrRecreate().fileImporter.state) {
                files[currentPosition].deckWhereToAdd
            }
        }
        return RenameDeckDialogState(
            abstractDeck,
            serializableState.typedDeckName
        )
    }
}