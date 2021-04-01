package com.odnovolov.forgetmenot.domain.interactor.autoplay

import com.odnovolov.forgetmenot.domain.doesCardMatchLastTestedFilter
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.flattenWithShallowShuffling
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.isCardAvailableForExercise
import kotlin.random.Random

class PlayerStateCreator(
    val state: State
) {
    data class State(
        val decks: List<Deck>,
        val cardFilter: CardFilterForAutoplay
    )

    fun getCurrentMatchingCardsNumber(): Int {
        return state.decks.sumBy { deck: Deck ->
            deck.cards
                .filter { card: Card -> doesCardMatchTheFilter(card, deck) }
                .count()
        }
    }

    fun hasAnyCardAvailableForAutoplay(): Boolean {
        return state.decks.any { deck: Deck ->
            deck.cards.any { card: Card ->
                doesCardMatchTheFilter(card, deck)
            }
        }
    }

    fun create(): Player.State {
        val playingCards: List<PlayingCard> = state.decks
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
                    .map { card: Card -> cardToPlayingCard(card, deck) }
            }
            .flattenWithShallowShuffling()
        return Player.State(
            playingCards = playingCards
        )
    }

    private fun doesCardMatchTheFilter(card: Card, deck: Deck): Boolean {
        return doesCardMatchStateFilter(card, deck)
                && card.grade in state.cardFilter.gradeRange
                && doesCardMatchLastTestedFilter(card, state.cardFilter)
    }

    private fun doesCardMatchStateFilter(card: Card, deck: Deck): Boolean {
        return when {
            card.isLearned -> state.cardFilter.areLearnedCardsIncluded
            isCardAvailableForExercise(card, deck.exercisePreference.intervalScheme) ->
                state.cardFilter.areCardsAvailableForExerciseIncluded
            else -> state.cardFilter.areAwaitingCardsIncluded
        }
    }

    private fun cardToPlayingCard(card: Card, deck: Deck): PlayingCard {
        val isInverted = when (deck.exercisePreference.cardInversion) {
            CardInversion.Off -> false
            CardInversion.On -> true
            CardInversion.EveryOtherLap -> (card.lap % 2) == 1
            CardInversion.Randomly -> Random.nextBoolean()
        }
        return PlayingCard(
            id = generateId(),
            card = card,
            deck = deck,
            isQuestionDisplayed = deck.exercisePreference.isQuestionDisplayed,
            isInverted = isInverted
        )
    }
}