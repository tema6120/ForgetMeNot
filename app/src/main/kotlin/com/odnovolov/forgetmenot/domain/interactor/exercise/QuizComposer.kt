package com.odnovolov.forgetmenot.domain.interactor.exercise

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import kotlin.random.Random

object QuizComposer {
    private val cache: MutableMap<Long, Array<Card>> = HashMap()

    fun compose(
        correctVariant: Card,
        deck: Deck,
        isReverse: Boolean,
        numberOfVariants: Int = 4,
        withCaching: Boolean
    ): List<Card?> {
        val result = ArrayList<Card?>(numberOfVariants).apply {
            add(correctVariant)
        }
        val cards: Array<Card> = cache[deck.id]
            ?: deck.cards.toTypedArray().also { cards: Array<Card> ->
                if (withCaching) cache[deck.id] = cards
            }
        val correctVariantIndex: Int = cards.indexOfFirst { it.id == correctVariant.id }
        cards.swap(0, correctVariantIndex)
        var attempts: Int = cards.size - 1
        while (result.size < numberOfVariants && attempts > 0) {
            val randomIndex: Int = Random.nextInt(
                from = cards.size - attempts,
                until = cards.size
            )
            val candidate: Card = cards[randomIndex]
            val isAppropriate: Boolean = result.all { resultVariant: Card? ->
                if (isReverse) {
                    resultVariant!!.question.trim() != candidate.question.trim()
                } else {
                    resultVariant!!.answer.trim() != candidate.answer.trim()
                }
            }
            if (isAppropriate) {
                result.add(candidate)
            }
            cards.swap(cards.size - attempts, randomIndex)
            attempts--
        }
        result.shuffle()
        while (result.size < numberOfVariants) {
            result.add(null)
        }
        return result
    }

    private fun <T> Array<T>.swap(i: Int, j: Int) {
        if (i == j) return
        val tmp = get(i)
        set(i, get(j))
        set(j, tmp)
    }

    // The same as in kotlin but without checking empty collection
    private inline fun <T> Iterable<T>.all(predicate: (T) -> Boolean): Boolean {
        for (element in this) if (!predicate(element)) return false
        return true
    }

    fun clearCache() {
        cache.clear()
    }
}