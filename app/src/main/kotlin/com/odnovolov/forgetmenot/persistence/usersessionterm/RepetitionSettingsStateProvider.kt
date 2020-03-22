package com.odnovolov.forgetmenot.persistence.usersessionterm

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionSettings
import com.odnovolov.forgetmenot.persistence.usersessionterm.RepetitionSettingsStateProvider.SerializableRepetitionSettingsState
import kotlinx.serialization.Serializable

class RepetitionSettingsStateProvider(
    private val globalState: GlobalState
) : BaseSerializableStateProvider<RepetitionSettings.State, SerializableRepetitionSettingsState>() {
    @Serializable
    data class SerializableRepetitionSettingsState(
        val deckIds: List<Long>,
        val isAvailableForExerciseCardsIncluded: Boolean,
        val isAwaitingCardsIncluded: Boolean,
        val isLearnedCardsIncluded: Boolean,
        val levelOfKnowledgeMin: Int,
        val levelOfKnowledgeMax: Int
    )

    override val serializer = SerializableRepetitionSettingsState.serializer()
    override val serializableClassName = SerializableRepetitionSettingsState::class.java.name

    override fun toSerializable(state: RepetitionSettings.State) =
        SerializableRepetitionSettingsState(
            deckIds = state.decks.map { it.id },
            isAvailableForExerciseCardsIncluded = state.isAvailableForExerciseCardsIncluded,
            isAwaitingCardsIncluded = state.isAwaitingCardsIncluded,
            isLearnedCardsIncluded = state.isLearnedCardsIncluded,
            levelOfKnowledgeMin = state.levelOfKnowledgeRange.first,
            levelOfKnowledgeMax = state.levelOfKnowledgeRange.last
        )

    override fun toOriginal(
        serializableState: SerializableRepetitionSettingsState
    ): RepetitionSettings.State {
        val decks: List<Deck> = globalState.decks.filter { it.id in serializableState.deckIds }
        val levelOfKnowledgeRange =
            serializableState.levelOfKnowledgeMin..serializableState.levelOfKnowledgeMax
        return RepetitionSettings.State(
            decks,
            serializableState.isAvailableForExerciseCardsIncluded,
            serializableState.isAwaitingCardsIncluded,
            serializableState.isLearnedCardsIncluded,
            levelOfKnowledgeRange
        )
    }
}