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

    private val repetitionSetting: RepetitionSetting get() = globalState.currentRepetitionSetting

    fun getCurrentMatchingCardsNumber(): Int {
        return state.decks.sumBy { deck: Deck ->
            deck.cards
                .filter { card: Card -> isCardMatchTheFilter(card, deck) }
                .count()
        }
    }

    fun hasAnyCardAvailableForRepetition(): Boolean {
        return state.decks.any { deck: Deck ->
            deck.cards.any { card: Card ->
                isCardMatchTheFilter(card, deck)
            }
        }
    }

    fun create(): Repetition.State {
        val repetitionCards: List<RepetitionCard> = state.decks
            .map { deck: Deck ->
                val isRandom = deck.exercisePreference.randomOrder
                deck.cards
                    .filter { card: Card -> isCardMatchTheFilter(card, deck) }
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
            numberOfLaps = repetitionSetting.numberOfLaps
        )
    }

    private fun isCardMatchTheFilter(card: Card, deck: Deck): Boolean {
        return isCorrespondingCardGroupIncluded(card, deck)
                && card.grade in repetitionSetting.gradeRange
                && isLastAnswerTimeInFilterRange(card)
    }

    private fun isCorrespondingCardGroupIncluded(card: Card, deck: Deck): Boolean {
        return when {
            card.isLearned -> repetitionSetting.isLearnedCardsIncluded
            isCardAvailableForExercise(card, deck.exercisePreference.intervalScheme) ->
                repetitionSetting.isAvailableForExerciseCardsIncluded
            else -> repetitionSetting.isAwaitingCardsIncluded
        }
    }

    private fun isLastAnswerTimeInFilterRange(card: Card): Boolean {
        val now = DateTime.now()
        return if (card.lastAnsweredAt == null) {
            repetitionSetting.lastAnswerFromTimeAgo == null
        } else {
            (repetitionSetting.lastAnswerFromTimeAgo == null
                    || card.lastAnsweredAt!! > now - repetitionSetting.lastAnswerFromTimeAgo!!)
                    &&
                    (repetitionSetting.lastAnswerToTimeAgo == null
                            || card.lastAnsweredAt!! < now - repetitionSetting.lastAnswerToTimeAgo!!)
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