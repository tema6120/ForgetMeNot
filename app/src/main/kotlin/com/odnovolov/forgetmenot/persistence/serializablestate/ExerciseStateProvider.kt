package com.odnovolov.forgetmenot.persistence.serializablestate

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.TestMethod
import com.odnovolov.forgetmenot.domain.entity.TestMethod.*
import com.odnovolov.forgetmenot.domain.interactor.exercise.*
import kotlinx.serialization.Serializable

object ExerciseStateProvider {
    fun load(globalState: GlobalState): Exercise.State {
        return loadSerializable(SerializableExerciseState.serializer())
            ?.toOriginal(globalState)
            ?: throw IllegalStateException("No Exercise.State in the Store")
    }

    fun save(exerciseState: Exercise.State) {
        val serializable: SerializableExerciseState = exerciseState.toSerializable()
        saveSerializable(serializable, SerializableExerciseState.serializer())
    }

    fun delete() {
        deleteSerializable(SerializableExerciseState::class)
    }

    @Serializable
    private data class SerializableExerciseState(
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
    private data class SerializableExerciseCard(
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
    private data class QuizAddition(
        val id: Long,
        val variantIds: List<Long?>,
        val selectedVariantIndex: Int?
    )

    @Serializable
    private data class EntryAddition(
        val id: Long,
        val userAnswer: String?
    )

    private fun Exercise.State.toSerializable(): SerializableExerciseState {
        val serializableExerciseCards: MutableList<SerializableExerciseCard> =
            ArrayList(exerciseCards.size)
        val quizAdditions: MutableList<QuizAddition> = ArrayList()
        val entryAdditions: MutableList<EntryAddition> = ArrayList()
        exerciseCards.forEach { exerciseCard: ExerciseCard ->
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
            currentPosition,
            questionSelection,
            answerSelection,
            hintSelection,
            isWalkingMode
        )
    }

    private fun SerializableExerciseState.toOriginal(globalState: GlobalState): Exercise.State {
        val deckIdDeckMap: Map<Long, Deck> = globalState.decks.associateBy { deck -> deck.id }
        val cardIdCardMap: Map<Long, Card> = globalState.decks
            .flatMap { deck -> deck.cards }
            .associateBy { card -> card.id }
        val exerciseCards: List<ExerciseCard> = serializableExerciseCards
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
                        val quizAddition: QuizAddition = quizAdditions
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
                        val entryAddition: EntryAddition = entryAdditions
                            .find { it.id == serializableExerciseCard.id }!!
                        EntryTestExerciseCard(baseExerciseCard, entryAddition.userAnswer)
                    }
                }
            }
        return Exercise.State(
            exerciseCards,
            currentPosition,
            questionSelection,
            answerSelection,
            hintSelection,
            isWalkingMode
        )
    }
}