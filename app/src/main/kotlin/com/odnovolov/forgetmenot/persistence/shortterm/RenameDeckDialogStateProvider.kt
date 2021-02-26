package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.persistence.shortterm.RenameDeckDialogStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDialogState
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDialogPurpose.*
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
        val purpose: SerializableRenameDeckDialogPurpose,
        val typedDeckName: String
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: RenameDeckDialogState): SerializableState {
        val serializablePurpose = when (val purpose = state.purpose) {
            is ToRenameExistingDeck ->
                SerializableRenameDeckDialogPurpose.ToRenameExistingDeck(purpose.deck.id)
            ToRenameNewDeckForFileImport ->
                SerializableRenameDeckDialogPurpose.ToRenameNewDeckForFileImport
            ToCreateNewDeck ->
                SerializableRenameDeckDialogPurpose.ToCreateNewDeck
            ToCreateNewForDeckChooser ->
                SerializableRenameDeckDialogPurpose.ToCreateNewForDeckChooser
        }
        return SerializableState(
            serializablePurpose,
            state.typedDeckName
        )
    }

    override fun toOriginal(serializableState: SerializableState): RenameDeckDialogState {
        val purpose = when (val purpose = serializableState.purpose) {
            is SerializableRenameDeckDialogPurpose.ToRenameExistingDeck -> {
                val deck: Deck = globalState.decks.first { deck: Deck -> deck.id == purpose.deckId }
                ToRenameExistingDeck(deck)
            }
            SerializableRenameDeckDialogPurpose.ToRenameNewDeckForFileImport -> {
                ToRenameNewDeckForFileImport
            }
            SerializableRenameDeckDialogPurpose.ToCreateNewDeck -> {
                ToCreateNewDeck
            }
            SerializableRenameDeckDialogPurpose.ToCreateNewForDeckChooser -> {
                ToCreateNewForDeckChooser
            }
        }
        return RenameDeckDialogState(
            purpose,
            serializableState.typedDeckName
        )
    }
}

@Serializable
sealed class SerializableRenameDeckDialogPurpose {
    @Serializable
    class ToRenameExistingDeck(val deckId: Long) : SerializableRenameDeckDialogPurpose()

    @Serializable
    object ToRenameNewDeckForFileImport : SerializableRenameDeckDialogPurpose()

    @Serializable
    object ToCreateNewDeck : SerializableRenameDeckDialogPurpose()

    @Serializable
    object ToCreateNewForDeckChooser : SerializableRenameDeckDialogPurpose()
}