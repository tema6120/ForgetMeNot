package com.odnovolov.forgetmenot.domain.interactor.repetition

import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.generateId

class RepetitionStateCreator(
    private val globalState: GlobalState
) {
    fun create(deckIds: List<Long>): Repetition.State {
        val repetitionCards: List<RepetitionCard> = globalState.decks
            .filter { deck -> deck.id in deckIds }
            .flatMap { deck: Deck ->
                deck.cards.map { card: Card -> cardToRepetitionCard(card, deck) }
            }
            .shuffled()
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