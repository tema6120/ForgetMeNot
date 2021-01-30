package com.odnovolov.forgetmenot.domain.interactor.autoplay

import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.flattenWithShallowShuffling
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.isCardAvailableForExercise
import com.soywiz.klock.DateTime
import kotlin.random.Random

class PlayerStateCreator(
    val state: State
) {
    data class State(
        val decks: List<Deck>,
        val cardFilterForAutoplay: CardFilterForAutoplay
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
        if (playingCards.isEmpty()) throw NoCardIsReadyForAutoplay
        return Player.State(
            playingCards = playingCards
        )
    }

    private fun doesCardMatchTheFilter(card: Card, deck: Deck): Boolean {
        return doesCardMatchStateFilter(card, deck)
                && card.grade in state.cardFilterForAutoplay.gradeRange
                && doesCardMatchLastTestedFilter(card)
    }

    private fun doesCardMatchStateFilter(card: Card, deck: Deck): Boolean {
        return when {
            card.isLearned -> state.cardFilterForAutoplay.areLearnedCardsIncluded
            isCardAvailableForExercise(card, deck.exercisePreference.intervalScheme) ->
                state.cardFilterForAutoplay.areCardsAvailableForExerciseIncluded
            else -> state.cardFilterForAutoplay.areAwaitingCardsIncluded
        }
    }

    private fun doesCardMatchLastTestedFilter(card: Card): Boolean {
        val now = DateTime.now()
        return if (card.lastTestedAt == null) {
            state.cardFilterForAutoplay.lastTestedFromTimeAgo == null
        } else {
            (state.cardFilterForAutoplay.lastTestedFromTimeAgo == null
                    || card.lastTestedAt!! > now - state.cardFilterForAutoplay.lastTestedFromTimeAgo!!)
                    &&
                    (state.cardFilterForAutoplay.lastTestedToTimeAgo == null
                            || card.lastTestedAt!! < now - state.cardFilterForAutoplay.lastTestedToTimeAgo!!)
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

    object NoCardIsReadyForAutoplay : Exception("no card is ready for autoplay")
}