package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.BatchCardEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.persistence.shortterm.BatchCardEditorProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseDiScope
import com.odnovolov.forgetmenot.presentation.screen.player.PlayerDiScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class BatchCardEditorProvider(
    json: Json,
    database: Database,
    private val globalState: GlobalState,
    override val key: String
) : BaseSerializableStateProvider<BatchCardEditor, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val serializableEditableCards: List<SerializableEditableCard>,
        val screen: EditingSpecificCardsScreen
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: BatchCardEditor): SerializableState {
        val batchCardEditor = state
        val serializableEditableCards: List<SerializableEditableCard> =
            batchCardEditor.state.selectedCards.map { editableCard: EditableCard ->
                editableCard.toSerializable()
            }
        val screen: EditingSpecificCardsScreen = when {
            batchCardEditor.exercise != null -> EditingSpecificCardsScreen.Exercise
            batchCardEditor.player != null -> EditingSpecificCardsScreen.Player
            else -> EditingSpecificCardsScreen.Other
        }
        return SerializableState(serializableEditableCards, screen)
    }

    override fun toOriginal(serializableState: SerializableState): BatchCardEditor {
        val deckIdDeckMap: Map<Long, Deck> = globalState.decks.associateBy { deck -> deck.id }
        val cardIdCardMap: Map<Long, Card> = globalState.decks
            .flatMap { deck -> deck.cards }
            .associateBy { card -> card.id }
        val selectedCards: Collection<EditableCard> = serializableState.serializableEditableCards
            .map { serializableEditableCard: SerializableEditableCard ->
                serializableEditableCard.toOriginal(deckIdDeckMap, cardIdCardMap)
            }
        val batchCardEditorState = BatchCardEditor.State(selectedCards)
        val exercise: Exercise? =
            if (serializableState.screen == EditingSpecificCardsScreen.Exercise) {
                ExerciseDiScope.getOrRecreate().exercise
            } else {
                null
            }
        val player: Player? =
            if (serializableState.screen == EditingSpecificCardsScreen.Player) {
                PlayerDiScope.getOrRecreate().player
            } else {
                null
            }
        return BatchCardEditor(
            globalState,
            batchCardEditorState,
            exercise,
            player
        )
    }
}