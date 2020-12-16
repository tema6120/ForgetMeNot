package com.odnovolov.forgetmenot.domain.interactor.autoplay

import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.flattenWithShallowShuffling
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.isCardAvailableForExercise
import com.soywiz.klock.DateTime

class PlayerStateCreator(
    val state: State,
    private val globalState: GlobalState
) {
    data class State(
        val decks: List<Deck>
    )

    private val cardFilterForAutoplay: CardFilterForAutoplay
        get() = globalState.cardFilterForAutoplay

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
                && card.grade in cardFilterForAutoplay.gradeRange
                && doesCardMatchLastTestedFilter(card)
    }

    private fun doesCardMatchStateFilter(card: Card, deck: Deck): Boolean {
        return when {
            card.isLearned -> cardFilterForAutoplay.isLearnedCardsIncluded
            isCardAvailableForExercise(card, deck.exercisePreference.intervalScheme) ->
                cardFilterForAutoplay.isAvailableForExerciseCardsIncluded
            else -> cardFilterForAutoplay.isAwaitingCardsIncluded
        }
    }

    private fun doesCardMatchLastTestedFilter(card: Card): Boolean {
        val now = DateTime.now()
        return if (card.lastAnsweredAt == null) {
            cardFilterForAutoplay.lastTestedFromTimeAgo == null
        } else {
            (cardFilterForAutoplay.lastTestedFromTimeAgo == null
                    || card.lastAnsweredAt!! > now - cardFilterForAutoplay.lastTestedFromTimeAgo!!)
                    &&
                    (cardFilterForAutoplay.lastTestedToTimeAgo == null
                            || card.lastAnsweredAt!! < now - cardFilterForAutoplay.lastTestedToTimeAgo!!)
        }
    }

    private fun cardToPlayingCard(card: Card, deck: Deck): PlayingCard {
        val isReverse = when (deck.exercisePreference.cardReverse) {
            CardReverse.Off -> false
            CardReverse.On -> true
            CardReverse.EveryOtherLap -> (card.lap % 2) == 1
        }
        return PlayingCard(
            id = generateId(),
            card = card,
            deck = deck,
            isQuestionDisplayed = deck.exercisePreference.isQuestionDisplayed,
            isReverse = isReverse
        )
    }

    object NoCardIsReadyForAutoplay : Exception("no card is ready for autoplay")
}