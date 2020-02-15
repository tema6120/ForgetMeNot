package com.odnovolov.forgetmenot.domain.interactor.exercise

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck

object QuizComposer {
    fun compose(
        correctVariant: Card,
        deck: Deck,
        isReverse: Boolean,
        numberOfVariants: Int = 4
    ): List<Card?> {
        val result = ArrayList<Card?>(numberOfVariants)
        result.add(correctVariant)
        val potentialVariants: MutableList<Card> = deck.cards.toMutableList()
        potentialVariants.remove(correctVariant)
        while (result.size < numberOfVariants && potentialVariants.isNotEmpty()) {
            val randomIndex = (0 until potentialVariants.size).random()
            val wrongVariant = potentialVariants.removeAt(randomIndex)
            val isAppropriate = result.all { resultVariant: Card? ->
                if (isReverse) {
                    resultVariant!!.question.trim() != wrongVariant.question.trim()
                } else {
                    resultVariant!!.answer.trim() != wrongVariant.answer.trim()
                }
            }
            if (isAppropriate) {
                result.add(wrongVariant)
            }
        }
        result.shuffle()
        while (result.size < numberOfVariants) {
            result.add(null)
        }
        return result
    }
}