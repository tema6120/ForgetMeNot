package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionCard
import com.odnovolov.forgetmenot.persistence.shortterm.RepetitionStateProvider.SerializableState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class RepetitionStateProvider(
    json: Json,
    database: Database,
    private val globalState: GlobalState,
    override val key: String = Repetition.State::class.qualifiedName!!
) : BaseSerializableStateProvider<Repetition.State, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val serializableRepetitionCards: List<SerializableRepetitionCard>,
        val repetitionCardPosition: Int,
        val speakEventPosition: Int,
        val isPlaying: Boolean,
        val numberOfLaps: Int,
        val currentLap: Int
    )

    @Serializable
    data class SerializableRepetitionCard(
        val id: Long,
        val cardId: Long,
        val deckId: Long,
        val isQuestionDisplayed: Boolean,
        val isReverse: Boolean,
        val isAnswered: Boolean
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: Repetition.State): SerializableState {
        val serializableRepetitionCards: List<SerializableRepetitionCard> = state.repetitionCards
            .map { repetitionCard: RepetitionCard ->
                with(repetitionCard) {
                    SerializableRepetitionCard(
                        id,
                        card.id,
                        deck.id,
                        isQuestionDisplayed,
                        isReverse,
                        isAnswered
                    )
                }
            }
        return SerializableState(
            serializableRepetitionCards,
            state.repetitionCardPosition,
            state.speakEventPosition,
            state.isPlaying,
            state.numberOfLaps,
            state.currentLap
        )
    }

    override fun toOriginal(serializableState: SerializableState): Repetition.State {
        val deckIdDeckMap: Map<Long, Deck> = globalState.decks.associateBy { deck -> deck.id }
        val cardIdCardMap: Map<Long, Card> = globalState.decks
            .flatMap { deck -> deck.cards }
            .associateBy { card -> card.id }
        val repetitionCards: List<RepetitionCard> = serializableState.serializableRepetitionCards
            .map { serializableRepetitionCard: SerializableRepetitionCard ->
                with(serializableRepetitionCard) {
                    RepetitionCard(
                        id = id,
                        card = cardIdCardMap.getValue(cardId),
                        deck = deckIdDeckMap.getValue(deckId),
                        isQuestionDisplayed = isQuestionDisplayed,
                        isAnswered = isAnswered,
                        isReverse = isReverse
                    )
                }
            }
        return Repetition.State(
            repetitionCards,
            serializableState.repetitionCardPosition,
            serializableState.speakEventPosition,
            serializableState.isPlaying,
            serializableState.numberOfLaps,
            serializableState.currentLap
        )
    }
}