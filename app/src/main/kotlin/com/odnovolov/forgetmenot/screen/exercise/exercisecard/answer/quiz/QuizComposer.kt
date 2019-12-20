package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.quiz

import com.odnovolov.forgetmenot.common.Card
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.exercise.exercisecards.ExerciseCardWithoutQuiz
import com.odnovolov.forgetmenot.exercise.exercisecards.QuizComposerQueries
import com.odnovolov.forgetmenot.exercise.exercisecards.TempQuiz

object QuizComposer {
    fun composeWhereItNeeds() {
        val queries: QuizComposerQueries = database.quizComposerQueries
        val exerciseCardsWithoutQuiz: List<ExerciseCardWithoutQuiz> = queries
            .exerciseCardWithoutQuiz()
            .executeAsList()
        if (exerciseCardsWithoutQuiz.isEmpty()) return
        val deckIds: List<Long> = exerciseCardsWithoutQuiz
            .map { it.deckId }
            .distinct()
        val cardsGroupedByDeckId: Map<Long, List<Card>> = queries
            .getCardsByDeckIds(deckIds)
            .executeAsList()
            .groupBy { it.deckId }
        for (exerciseCardWithoutQuiz: ExerciseCardWithoutQuiz in exerciseCardsWithoutQuiz) {
            val actualCards: List<Card> = cardsGroupedByDeckId[exerciseCardWithoutQuiz.deckId]!!
            val correctVariant: Card = actualCards.find { it.id == exerciseCardWithoutQuiz.cardId }!!
            val variants: List<Card> = actualCards
                .minus(correctVariant)
                .filter { it.answer != correctVariant.answer }
                .distinctBy { it.answer }
                .shuffled()
                .take(3)
                .plus(correctVariant)
                .shuffled()
            val quiz = TempQuiz.Impl(
                exerciseCardId = exerciseCardWithoutQuiz.exerciseCardId,
                variant1CardId = variants.getOrNull(0)?.id,
                variant2CardId = variants.getOrNull(1)?.id,
                variant3CardId = variants.getOrNull(2)?.id,
                variant4CardId = variants.getOrNull(3)?.id,
                selectedVariant = null
            )
            queries.addQuiz(quiz)
        }
    }
}