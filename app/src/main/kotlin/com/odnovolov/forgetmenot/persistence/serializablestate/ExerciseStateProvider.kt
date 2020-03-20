package com.odnovolov.forgetmenot.persistence.serializablestate

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.TestMethod
import com.odnovolov.forgetmenot.domain.entity.TestMethod.*
import com.odnovolov.forgetmenot.domain.interactor.exercise.*
import com.odnovolov.forgetmenot.persistence.serializablestate.ExerciseStateProvider.SerializableExerciseState
import kotlinx.serialization.Serializable

class ExerciseStateProvider(
    private val globalState: GlobalState
) : BaseSerializableStateProvider<Exercise.State, SerializableExerciseState>() {
    @Serializable
    data class SerializableExerciseState(
        val serializableExerciseCards: List<SerializableExerciseCard>,
        val quizAdditions: List<QuizAddition>,
        val entryAdditions: List<EntryAddition>,
        val currentPosition: Int,
        val questionSelection: String,
        val answerSelection: String,
        val hintSelection: HintSelection,
        val isWalkingMode: Boolean
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
        val initialLevelOfKnowledge: Int,
        val isLevelOfKnowledgeEditedManually: Boolean,
        val testMethod: TestMethod
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
        val userAnswer: String?
    )

    override val serializer = SerializableExerciseState.serializer()
    override val serializableClassName = SerializableExerciseState::class.java.name

    override fun toSerializable(state: Exercise.State): SerializableExerciseState {
        val serializableExerciseCards: MutableList<SerializableExerciseCard> =
            ArrayList(state.exerciseCards.size)
        val quizAdditions: MutableList<QuizAddition> = ArrayList()
        val entryAdditions: MutableList<EntryAddition> = ArrayList()
        state.exerciseCards.forEach { exerciseCard: ExerciseCard ->
            val testMethod: TestMethod = when (exerciseCard) {
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
                        userAnswer = exerciseCard.userAnswer
                    )
                    entryAdditions.add(entryAddition)
                    Entry
                }
                else -> throw AssertionError()
            }
            val serializableExerciseCard = with(exerciseCard.base) {
                SerializableExerciseCard(
                    id = id,
                    cardId = card.id,
                    deckId = deck.id,
                    isReverse = isReverse,
                    isQuestionDisplayed = isQuestionDisplayed,
                    isAnswerCorrect = isAnswerCorrect,
                    hint = hint,
                    initialLevelOfKnowledge = initialLevelOfKnowledge,
                    isLevelOfKnowledgeEditedManually = isLevelOfKnowledgeEditedManually,
                    testMethod = testMethod
                )
            }
            serializableExerciseCards.add(serializableExerciseCard)
        }
        return SerializableExerciseState(
            serializableExerciseCards,
            quizAdditions,
            entryAdditions,
            state.currentPosition,
            state.questionSelection,
            state.answerSelection,
            state.hintSelection,
            state.isWalkingMode
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
                        id = id,
                        card = cardIdCardMap.getValue(cardId),
                        deck = deckIdDeckMap.getValue(deckId),
                        isReverse = isReverse,
                        isQuestionDisplayed = isQuestionDisplayed,
                        isAnswerCorrect = isAnswerCorrect,
                        hint = hint,
                        initialLevelOfKnowledge = initialLevelOfKnowledge,
                        isLevelOfKnowledgeEditedManually = isLevelOfKnowledgeEditedManually
                    )
                }
                when (serializableExerciseCard.testMethod) {
                    Off -> OffTestExerciseCard(baseExerciseCard) as ExerciseCard
                    Manual -> ManualTestExerciseCard(baseExerciseCard)
                    Quiz -> {
                        val quizAddition: QuizAddition = serializableState.quizAdditions
                            .find { it.id == serializableExerciseCard.id }!!
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
                            .find { it.id == serializableExerciseCard.id }!!
                        EntryTestExerciseCard(baseExerciseCard, entryAddition.userAnswer)
                    }
                }
            }
        return Exercise.State(
            exerciseCards,
            serializableState.currentPosition,
            serializableState.questionSelection,
            serializableState.answerSelection,
            serializableState.hintSelection,
            serializableState.isWalkingMode
        )
    }
}