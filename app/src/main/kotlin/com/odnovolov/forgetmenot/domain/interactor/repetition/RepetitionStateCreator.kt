package com.odnovolov.forgetmenot.domain.interactor.repetition

import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.flattenWithShallowShuffling
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.isCardAvailableForExercise
import com.soywiz.klock.DateTime

class RepetitionStateCreator(
    val state: State,
    private val globalState: GlobalState
) {
    data class State(
        val decks: List<Deck>
    )

    private val cardFiltersForAutoplay: CardFiltersForAutoplay
        get() = globalState.cardFiltersForAutoplay

    fun getCurrentMatchingCardsNumber(): Int {
        return state.decks.sumBy { deck: Deck ->
            deck.cards
                .filter { card: Card -> doesCardMatchTheFilter(card, deck) }
                .count()
        }
    }

    fun hasAnyCardAvailableForRepetition(): Boolean {
        return state.decks.any { deck: Deck ->
            deck.cards.any { card: Card ->
                doesCardMatchTheFilter(card, deck)
            }
        }
    }

    fun create(): Repetition.State {
        val repetitionCards: List<RepetitionCard> = state.decks
            .map { deck: Deck ->
                val isRandom = deck.exercisePreference.randomOrder
                deck.cards
                    .filter { card: Card -> doesCardMatchTheFilter(card, deck) }
                    .let { cards: List<Card> ->
                        if (isRandom)
                            cards.shuffled()
                        else
                            cards.sortedBy { it.lap }
                    }
                    .map { card: Card -> cardToRepetitionCard(card, deck) }
            }
            .flattenWithShallowShuffling()
        if (repetitionCards.isEmpty()) throw NoCardIsReadyForRepetition
        return Repetition.State(
            repetitionCards = repetitionCards,
            numberOfLaps = 1
        )
    }

    private fun doesCardMatchTheFilter(card: Card, deck: Deck): Boolean {
        return doesCardMatchStateFilter(card, deck)
                && card.grade in cardFiltersForAutoplay.gradeRange
                && doesCardMatchLastTestedFilter(card)
    }

    private fun doesCardMatchStateFilter(card: Card, deck: Deck): Boolean {
        return when {
            card.isLearned -> cardFiltersForAutoplay.isLearnedCardsIncluded
            isCardAvailableForExercise(card, deck.exercisePreference.intervalScheme) ->
                cardFiltersForAutoplay.isAvailableForExerciseCardsIncluded
            else -> cardFiltersForAutoplay.isAwaitingCardsIncluded
        }
    }

    private fun doesCardMatchLastTestedFilter(card: Card): Boolean {
        val now = DateTime.now()
        return if (card.lastAnsweredAt == null) {
            cardFiltersForAutoplay.lastTestedFromTimeAgo == null
        } else {
            (cardFiltersForAutoplay.lastTestedFromTimeAgo == null
                    || card.lastAnsweredAt!! > now - cardFiltersForAutoplay.lastTestedFromTimeAgo!!)
                    &&
                    (cardFiltersForAutoplay.lastTestedToTimeAgo == null
                            || card.lastAnsweredAt!! < now - cardFiltersForAutoplay.lastTestedToTimeAgo!!)
        }
    }

    private fun cardToRepetitionCard(card: Card, deck: Deck): RepetitionCard {
        val isReverse = when (deck.exercisePreference.cardReverse) {
            CardReverse.Off -> false
            CardReverse.On -> true
            CardReverse.EveryOtherLap -> (card.lap % 2) == 1
        }
        return RepetitionCard(
            id = generateId(),
            card = card,
            deck = deck,
            isQuestionDisplayed = deck.exercisePreference.isQuestionDisplayed,
            isReverse = isReverse
        )
    }

    object NoCardIsReadyForRepetition : Exception("no card is ready for repetition")
}