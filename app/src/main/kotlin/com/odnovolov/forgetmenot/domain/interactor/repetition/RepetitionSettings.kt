package com.odnovolov.forgetmenot.domain.interactor.repetition

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.flattenWithShallowShuffling
import com.odnovolov.forgetmenot.domain.generateId

class RepetitionSettings(
    val state: State
) {
    class State(
        decks: List<Deck>,
        levelOfKnowledgeRange: IntRange = run {
            val allLevelOfKnowledge: List<Int> = decks
                .flatMap { it.cards }
                .map { it.levelOfKnowledge }
            val min: Int = allLevelOfKnowledge.min()!!
            val max: Int = allLevelOfKnowledge.max()!!
            min..max
        }
    ) : FlowableState<State>() {
        val decks: List<Deck> by me(decks)
        var levelOfKnowledgeRange: IntRange by me(levelOfKnowledgeRange)
    }

    fun setLevelOfKnowledgeRange(levelOfKnowledgeRange: IntRange) {
        state.levelOfKnowledgeRange = levelOfKnowledgeRange
    }

    fun createRepetitionState(): Repetition.State {
        val repetitionCards: List<RepetitionCard> = state.decks
            .map { deck: Deck ->
                val isRandom = deck.exercisePreference.randomOrder
                deck.cards
                    .filter { card: Card -> card.levelOfKnowledge in state.levelOfKnowledgeRange }
                    .let { cards: List<Card> -> if (isRandom) cards.shuffled() else cards }
                    .sortedBy { card: Card -> card.lap }
                    .map { card: Card -> cardToRepetitionCard(card, deck) }
            }
            .flattenWithShallowShuffling()
        return Repetition.State(repetitionCards)
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
}