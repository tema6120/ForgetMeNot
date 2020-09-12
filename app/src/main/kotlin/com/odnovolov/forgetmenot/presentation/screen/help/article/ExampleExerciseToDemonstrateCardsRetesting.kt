package com.odnovolov.forgetmenot.presentation.screen.help.article

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.generateId

class ExampleExerciseToDemonstrateCardsRetesting(
    val state: State
) {
    class State(
        exerciseCards: List<ExerciseCard>
    ) : FlowMaker<State>() {
        var exerciseCards: List<ExerciseCard> by flowMaker(exerciseCards)
    }

    class ExerciseCard(
        id: Long = generateId(),
        card: Card,
        isAnswerCorrect: Boolean? = null
    ) : FlowMaker<ExerciseCard>() {
        val id: Long by flowMaker(id)
        val card: Card by flowMaker(card)
        var isAnswerCorrect: Boolean? by flowMaker(isAnswerCorrect)
    }

    class Card(
        id: Long = generateId(),
        question: String,
        answer: String,
        levelOfKnowledge: Int,
        initialLevelOfKnowledge: Int = levelOfKnowledge,
        isLevelOfKnowledgeEditedManually: Boolean = false
    ) : FlowMaker<Card>() {
        val id: Long by flowMaker(id)
        val question: String by flowMaker(question)
        val answer: String by flowMaker(answer)
        var levelOfKnowledge: Int by flowMaker(levelOfKnowledge)
        val initialLevelOfKnowledge: Int by flowMaker(initialLevelOfKnowledge)
        var isLevelOfKnowledgeEditedManually: Boolean by flowMaker(isLevelOfKnowledgeEditedManually)
    }

    fun setLevelOfKnowledge(levelOfKnowledge: Int, exerciseCard: ExerciseCard) {
        if (levelOfKnowledge < 0) return
        exerciseCard.card.apply {
            this.levelOfKnowledge = levelOfKnowledge
            isLevelOfKnowledgeEditedManually = true
        }
    }

    fun setAnswerAsCorrect(exerciseCard: ExerciseCard) {
        handleAnswer(exerciseCard, isAnswerCorrect = true)
    }

    fun setAnswerAsWrong(exerciseCard: ExerciseCard) {
        handleAnswer(exerciseCard, isAnswerCorrect = false)
    }

    private fun handleAnswer(exerciseCard: ExerciseCard, isAnswerCorrect: Boolean) {
        if (exerciseCard.isAnswerCorrect == isAnswerCorrect) return
        exerciseCard.isAnswerCorrect = isAnswerCorrect
        if (isAnswerCorrect) {
            deleteCardsForRetesting(exerciseCard)
        } else {
            addExerciseCardToRetestIfNeed(exerciseCard)
        }
        updateLevelOfKnowledge(exerciseCard)
    }

    private fun deleteCardsForRetesting(currentExerciseCard: ExerciseCard) {
        val exerciseCardPosition: Int =
            state.exerciseCards.indexOfFirst { it.id == currentExerciseCard.id }
        if (hasExerciseCardForRetesting(exerciseCardPosition, currentExerciseCard.card.id)) {
            state.exerciseCards = state.exerciseCards
                .filterIndexed { index, exerciseCard ->
                    exerciseCard.card.id != currentExerciseCard.card.id
                            ||
                            exerciseCard.card.id == currentExerciseCard.card.id
                            && index <= exerciseCardPosition
                }
        }
    }

    private fun addExerciseCardToRetestIfNeed(currentExerciseCard: ExerciseCard) {
        val exerciseCardPosition: Int =
            state.exerciseCards.indexOfFirst { it.id == currentExerciseCard.id }
        if (hasExerciseCardForRetesting(exerciseCardPosition, currentExerciseCard.card.id)) return
        val retestingExerciseCard = ExerciseCard(card = currentExerciseCard.card)
        state.exerciseCards += retestingExerciseCard
    }

    private fun hasExerciseCardForRetesting(exerciseCardPosition: Int, cardId: Long): Boolean {
        return state.exerciseCards
            .drop(exerciseCardPosition + 1)
            .any { it.card.id == cardId }
    }

    private fun updateLevelOfKnowledge(currentExerciseCard: ExerciseCard) {
        if (currentExerciseCard.card.isLevelOfKnowledgeEditedManually) return
        var numberOfCorrect = 0
        var numberOfWrong = 0
        for (exerciseCard in state.exerciseCards) {
            if (exerciseCard.card.id != currentExerciseCard.card.id) continue
            when (exerciseCard.isAnswerCorrect) {
                true -> numberOfCorrect++
                false -> numberOfWrong++
            }
        }
        with(currentExerciseCard.card) {
            levelOfKnowledge = when {
                numberOfWrong > 0 -> maxOf(0, initialLevelOfKnowledge - numberOfWrong)
                numberOfCorrect > 0 -> initialLevelOfKnowledge + 1
                else -> return
            }
        }
    }
}
