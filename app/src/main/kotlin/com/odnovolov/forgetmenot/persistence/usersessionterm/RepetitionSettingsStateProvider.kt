package com.odnovolov.forgetmenot.persistence.usersessionterm

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionSettings
import com.odnovolov.forgetmenot.persistence.dateTimeSpanAdapter
import com.odnovolov.forgetmenot.persistence.usersessionterm.RepetitionSettingsStateProvider.SerializableRepetitionSettingsState
import com.soywiz.klock.DateTimeSpan
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
        val levelOfKnowledgeMax: Int,
        val lastAnswerFromTimeAgo: String?,
        val lastAnswerToTimeAgo: String?
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
            levelOfKnowledgeMax = state.levelOfKnowledgeRange.last,
            lastAnswerFromTimeAgo = state.lastAnswerFromTimeAgo?.let(dateTimeSpanAdapter::encode),
            lastAnswerToTimeAgo = state.lastAnswerToTimeAgo?.let(dateTimeSpanAdapter::encode)
        )

    override fun toOriginal(
        serializableState: SerializableRepetitionSettingsState
    ): RepetitionSettings.State {
        val decks: List<Deck> = globalState.decks.filter { it.id in serializableState.deckIds }
        val levelOfKnowledgeRange: IntRange =
            serializableState.levelOfKnowledgeMin..serializableState.levelOfKnowledgeMax
        val lastAnswerFromTimeAgo: DateTimeSpan? =
            serializableState.lastAnswerFromTimeAgo?.let(dateTimeSpanAdapter::decode)
        val lastAnswerToTimeAgo: DateTimeSpan? =
            serializableState.lastAnswerToTimeAgo?.let(dateTimeSpanAdapter::decode)
        return RepetitionSettings.State(
            decks,
            serializableState.isAvailableForExerciseCardsIncluded,
            serializableState.isAwaitingCardsIncluded,
            serializableState.isLearnedCardsIncluded,
            levelOfKnowledgeRange,
            lastAnswerFromTimeAgo,
            lastAnswerToTimeAgo
        )
    }
}