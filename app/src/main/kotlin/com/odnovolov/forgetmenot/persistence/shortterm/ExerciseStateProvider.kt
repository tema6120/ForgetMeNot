package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.TestingMethod
import com.odnovolov.forgetmenot.domain.entity.TestingMethod.*
import com.odnovolov.forgetmenot.domain.interactor.exercise.*
import com.odnovolov.forgetmenot.persistence.shortterm.ExerciseStateProvider.SerializableExerciseState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ExerciseStateProvider(
    json: Json,
    database: Database,
    private val globalState: GlobalState,
    override val key: String = Exercise.State::class.qualifiedName!!
) : BaseSerializableStateProvider<Exercise.State, SerializableExerciseState>(
    json,
    database
) {
    @Serializable
    data class SerializableExerciseState(
        val serializableExerciseCards: List<SerializableExerciseCard>,
        val quizAdditions: List<QuizAddition>,
        val entryAdditions: List<EntryAddition>,
        val currentPosition: Int
    )

    @Serializable
    data class SerializableExerciseCard(
        val id: Long,
        val cardId: Long,
        val deckId: Long,
        val isReverse: Boolean,
        val isQuestionDisplayed: Boolean,
        val isAnswerCorrect: Boolean?,
        val hint: String?,
        val timeLeft: Int,
        val isExpired: Boolean = false,
        val initialGrade: Int,
        val isGradeEditedManually: Boolean,
        val testingMethod: TestingMethod
    )

    @Serializable
    data class QuizAddition(
        val id: Long,
        val variantIds: List<Long?>,
        val selectedVariantIndex: Int?
    )

    @Serializable
    data class EntryAddition(
        val id: Long,
        val userInput: String?
    )

    override val serializer = SerializableExerciseState.serializer()

    override fun toSerializable(state: Exercise.State): SerializableExerciseState {
        val serializableExerciseCards: MutableList<SerializableExerciseCard> =
            ArrayList(state.exerciseCards.size)
        val quizAdditions: MutableList<QuizAddition> = ArrayList()
        val entryAdditions: MutableList<EntryAddition> = ArrayList()
        state.exerciseCards.forEach { exerciseCard: ExerciseCard ->
            val testingMethod: TestingMethod = when (exerciseCard) {
                is OffTestExerciseCard -> Off
                is ManualTestExerciseCard -> Manual
                is QuizTestExerciseCard -> {
                    val variantIds: List<Long?> = exerciseCard.variants.map { it?.id }
                    val quizAddition = QuizAddition(
                        id = exerciseCard.base.id,
                        variantIds = variantIds,
                        selectedVariantIndex = exerciseCard.selectedVariantIndex
                    )
                    quizAdditions.add(quizAddition)
                    Quiz
                }
                is EntryTestExerciseCard -> {
                    val entryAddition = EntryAddition(
                        id = exerciseCard.base.id,
                        userInput = exerciseCard.userInput
                    )
                    entryAdditions.add(entryAddition)
                    Entry
                }
                else -> throw AssertionError()
            }
            val serializableExerciseCard = with(exerciseCard.base) {
                SerializableExerciseCard(
                    id,
                    card.id,
                    deck.id,
                    isInverted,
                    isQuestionDisplayed,
                    isAnswerCorrect,
                    hint,
                    timeLeft,
                    isExpired,
                    initialGrade,
                    isGradeEditedManually,
                    testingMethod
                )
            }
            serializableExerciseCards.add(serializableExerciseCard)
        }
        return SerializableExerciseState(
            serializableExerciseCards,
            quizAdditions,
            entryAdditions,
            state.currentPosition
        )
    }

    override fun toOriginal(serializableState: SerializableExerciseState): Exercise.State {
        val deckIdDeckMap: Map<Long, Deck> = globalState.decks.associateBy { deck -> deck.id }
        val cardIdCardMap: Map<Long, Card> = globalState.decks
            .flatMap { deck -> deck.cards }
            .associateBy { card -> card.id }
        val exerciseCards: List<ExerciseCard> = serializableState.serializableExerciseCards
            .map { serializableExerciseCard: SerializableExerciseCard ->
                val baseExerciseCard = with(serializableExerciseCard) {
                    ExerciseCard.Base(
                        id,
                        cardIdCardMap.getValue(cardId),
                        deckIdDeckMap.getValue(deckId),
                        isReverse,
                        isQuestionDisplayed,
                        isAnswerCorrect,
                        hint,
                        timeLeft,
                        isExpired,
                        initialGrade,
                        isGradeEditedManually
                    )
                }
                when (serializableExerciseCard.testingMethod) {
                    Off -> OffTestExerciseCard(baseExerciseCard)
                    Manual -> ManualTestExerciseCard(baseExerciseCard)
                    Quiz -> {
                        val quizAddition: QuizAddition = serializableState.quizAdditions
                            .first { it.id == serializableExerciseCard.id }
                        val variants: List<Card?> = quizAddition.variantIds
                            .map { variantId: Long? ->
                                variantId?.let {
                                    cardIdCardMap.getValue(variantId)
                                }
                            }
                        QuizTestExerciseCard(
                            baseExerciseCard,
                            variants,
                            quizAddition.selectedVariantIndex
                        )
                    }
                    Entry -> {
                        val entryAddition: EntryAddition = serializableState.entryAdditions
                            .first { it.id == serializableExerciseCard.id }
                        EntryTestExerciseCard(baseExerciseCard, entryAddition.userInput)
                    }
                }
            }
        return Exercise.State(
            exerciseCards,
            serializableState.currentPosition
        )
    }
}