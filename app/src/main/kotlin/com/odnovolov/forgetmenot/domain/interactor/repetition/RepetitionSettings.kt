package com.odnovolov.forgetmenot.domain.interactor.repetition

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.CardReverse
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.SpeakPlan
import com.odnovolov.forgetmenot.domain.flattenWithShallowShuffling
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.isCardAvailableForExercise
import com.soywiz.klock.DateTime
import com.soywiz.klock.DateTimeSpan

class RepetitionSettings(
    val state: State
) {
    class State(
        decks: List<Deck>,
        isAvailableForExerciseCardsIncluded: Boolean = false,
        isAwaitingCardsIncluded: Boolean = true,
        isLearnedCardsIncluded: Boolean = false,
        levelOfKnowledgeRange: IntRange = run {
            val allLevelOfKnowledge: List<Int> = decks
                .flatMap { it.cards }
                .map { it.levelOfKnowledge }
            val min: Int = allLevelOfKnowledge.min()!!
            val max: Int = allLevelOfKnowledge.max()!!
            min..max
        },
        lastAnswerFromTimeAgo: DateTimeSpan? = null,
        lastAnswerToTimeAgo: DateTimeSpan? = null,
        numberOfLaps: Int = 1
    ) : FlowableState<State>() {
        val decks: List<Deck> by me(decks)
        var isAvailableForExerciseCardsIncluded: Boolean by me(isAvailableForExerciseCardsIncluded)
        var isAwaitingCardsIncluded: Boolean by me(isAwaitingCardsIncluded)
        var isLearnedCardsIncluded: Boolean by me(isLearnedCardsIncluded)
        var levelOfKnowledgeRange: IntRange by me(levelOfKnowledgeRange)
        var lastAnswerFromTimeAgo: DateTimeSpan? by me(lastAnswerFromTimeAgo) // null means zero time
        var lastAnswerToTimeAgo: DateTimeSpan? by me(lastAnswerToTimeAgo) // null means now
        var numberOfLaps: Int by me(numberOfLaps)
    }

    fun setIsAvailableForExerciseCardsIncluded(isIncluded: Boolean) {
        state.isAvailableForExerciseCardsIncluded = isIncluded
    }

    fun setIsAwaitingCardsIncluded(isIncluded: Boolean) {
        state.isAwaitingCardsIncluded = isIncluded
    }

    fun setIsLearnedCardsIncluded(isIncluded: Boolean) {
        state.isLearnedCardsIncluded = isIncluded
    }

    fun setLevelOfKnowledgeRange(levelOfKnowledgeRange: IntRange) {
        state.levelOfKnowledgeRange = levelOfKnowledgeRange
    }

    fun setLastAnswerFromTimeAgo(lastAnswerFromTimeAgo: DateTimeSpan?) {
        state.lastAnswerFromTimeAgo = lastAnswerFromTimeAgo
    }

    fun setLastAnswerToTimeAgo(lastAnswerToTimeAgo: DateTimeSpan?) {
        state.lastAnswerToTimeAgo = lastAnswerToTimeAgo
    }

    fun setNumberOfLaps(numberOfLaps: Int) {
        state.numberOfLaps = numberOfLaps
    }

    fun createRepetitionState(): Repetition.State {
        if (state.numberOfLaps <= 0) throw WrongNumberOfLaps
        val repetitionCards: List<RepetitionCard> = state.decks
            .map { deck: Deck ->
                val isRandom = deck.exercisePreference.randomOrder
                deck.cards
                    .filter { card: Card ->
                        isCorrespondingCardGroupIncluded(card, deck)
                                && card.levelOfKnowledge in state.levelOfKnowledgeRange
                                && isLastAnswerTimeInFilterRange(card)
                    }
                    .let { cards: List<Card> -> if (isRandom) cards.shuffled() else cards }
                    .sortedBy { card: Card -> card.lap }
                    .map { card: Card -> cardToRepetitionCard(card, deck) }
            }
            .flattenWithShallowShuffling()
        if (repetitionCards.isEmpty()) throw NoCardIsReadyForRepetition
        return Repetition.State(
            repetitionCards = repetitionCards,
            numberOfLaps = state.numberOfLaps
        )
    }

    private fun isCorrespondingCardGroupIncluded(card: Card, deck: Deck): Boolean {
        return when {
            card.isLearned -> state.isLearnedCardsIncluded
            isCardAvailableForExercise(card, deck.exercisePreference.intervalScheme) ->
                state.isAvailableForExerciseCardsIncluded
            else -> state.isAwaitingCardsIncluded
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
            isReverse = isReverse,
            pronunciation = deck.exercisePreference.pronunciation,
            speakPlan = SpeakPlan.Default
        )
    }

    private fun isLastAnswerTimeInFilterRange(card: Card): Boolean {
        val now = DateTime.now()
        return if (card.lastAnsweredAt == null) {
            state.lastAnswerFromTimeAgo == null
        } else {
            (state.lastAnswerFromTimeAgo == null
                    || card.lastAnsweredAt!! > now - state.lastAnswerFromTimeAgo!!)
                    &&
                    (state.lastAnswerToTimeAgo == null
                            || card.lastAnsweredAt!! < now - state.lastAnswerToTimeAgo!!)
        }
    }

    object NoCardIsReadyForRepetition : Exception("no card is ready for repetition")
    object WrongNumberOfLaps : Exception("number of laps should be greater than zero")
}