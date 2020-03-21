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
        val levelOfKnowledgeMin: Int,
        val levelOfKnowledgeMax: Int
    )

    override val serializer = SerializableRepetitionSettingsState.serializer()
    override val serializableClassName = SerializableRepetitionSettingsState::class.java.name

    override fun toSerializable(state: RepetitionSettings.State) =
        SerializableRepetitionSettingsState(
            deckIds = state.decks.map { it.id },
            levelOfKnowledgeMin = state.levelOfKnowledgeRange.first,
            levelOfKnowledgeMax = state.levelOfKnowledgeRange.last
        )

    override fun toOriginal(
        serializableState: SerializableRepetitionSettingsState
    ): RepetitionSettings.State {
        val decks: List<Deck> = globalState.decks.filter { it.id in serializableState.deckIds }
        val levelOfKnowledgeRange =
            serializableState.levelOfKnowledgeMin..serializableState.levelOfKnowledgeMax
        return RepetitionSettings.State(decks, levelOfKnowledgeRange)
    }
}