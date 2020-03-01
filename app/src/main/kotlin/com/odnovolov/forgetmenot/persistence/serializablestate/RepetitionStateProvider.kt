package com.odnovolov.forgetmenot.persistence.serializablestate

import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionCard
import kotlinx.serialization.Serializable

object RepetitionStateProvider {
    fun load(globalState: GlobalState): Repetition.State {
        return loadSerializable(SerializableRepetitionState.serializer())
            ?.toOriginal(globalState)
            ?: throw  IllegalStateException("No Repetition.State in the Store")
    }

    fun save(state: Repetition.State) {
        val serializable: SerializableRepetitionState = state.toSerializable()
        saveSerializable(serializable, SerializableRepetitionState.serializer())
    }

    fun delete() {
        deleteSerializable(SerializableRepetitionState::class)
    }

    @Serializable
    private data class SerializableRepetitionState(
        val serializableRepetitionCards: List<SerializableRepetitionCard>,
        val repetitionCardPosition: Int,
        val speakEventPosition: Int,
        val isPlaying: Boolean
    )

    @Serializable
    private data class SerializableRepetitionCard(
        val id: Long,
        val cardId: Long,
        val isAnswered: Boolean,
        val isReverse: Boolean,
        val pronunciationId: Long,
        val speakPlanId: Long
    )

    private fun Repetition.State.toSerializable(): SerializableRepetitionState {
        val serializableRepetitionCards: List<SerializableRepetitionCard> = repetitionCards
            .map { repetitionCard: RepetitionCard ->
                with(repetitionCard) {
                    SerializableRepetitionCard(
                        id = id,
                        cardId = card.id,
                        isAnswered = isAnswered,
                        isReverse = isReverse,
                        pronunciationId = pronunciation.id,
                        speakPlanId = speakPlan.id
                    )
                }
            }
        return SerializableRepetitionState(
            serializableRepetitionCards,
            repetitionCardPosition,
            speakEventPosition,
            isPlaying
        )
    }

    private fun SerializableRepetitionState.toOriginal(globalState: GlobalState): Repetition.State {
        val cardMap: Map<Long, Card> = globalState.decks
            .flatMap { deck -> deck.cards }
            .associateBy { card -> card.id }
        val pronunciationMap: Map<Long, Pronunciation> =
            globalState.sharedPronunciations
                .plus(globalState.decks.map { deck: Deck -> deck.exercisePreference.pronunciation })
                .associateBy { pronunciation: Pronunciation -> pronunciation.id }
        val repetitionCards: List<RepetitionCard> = serializableRepetitionCards
            .map { serializableRepetitionCard: SerializableRepetitionCard ->
                with(serializableRepetitionCard) {
                    RepetitionCard(
                        id = id,
                        card = cardMap.getValue(cardId),
                        isAnswered = isAnswered,
                        isReverse = isReverse,
                        pronunciation = pronunciationMap.getValue(pronunciationId),
                        speakPlan = SpeakPlan.Default // todo
                    )
                }
            }
        return Repetition.State(
            repetitionCards,
            repetitionCardPosition,
            speakEventPosition,
            isPlaying
        )
    }
}