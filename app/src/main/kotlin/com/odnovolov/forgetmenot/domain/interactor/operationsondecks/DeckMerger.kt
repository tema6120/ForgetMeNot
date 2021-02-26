package com.odnovolov.forgetmenot.domain.interactor.operationsondecks

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableList
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.operationsondecks.DeckMerger.MergeData
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class DeckMerger(
    private val globalState: GlobalState,
    coroutineContext: CoroutineContext,
    private val timeToKeepBackup: Long = 10_000L
) {
    data class MergeData(
        val decks: List<Deck>,
        val destination: AbstractDeck
    )

    private val coroutineScope = CoroutineScope(coroutineContext)
    private var backupKeeping: Job? = null
    private var restore: (() -> Unit)? = null

    fun merge(mergeData: MergeData): Int {
        val (decks: List<Deck>, abstractDeck: AbstractDeck) = mergeData
        if (decks.isEmpty()) return 0
        return when (abstractDeck) {
            is ExistingDeck -> mergeIntoExistingDeck(decks, abstractDeck.deck)
            is NewDeck -> mergeIntoNewDeck(decks, abstractDeck.deckName)
            else -> error(ERROR_MESSAGE_UNKNOWN_IMPLEMENTATION_OF_ABSTRACT_DECK)
        }
    }

    private fun mergeIntoExistingDeck(decks: List<Deck>, destination: Deck): Int {
        backupKeeping?.cancel()
        val decksToMerge = decks.filter { deck: Deck -> deck.id != destination.id }
        if (decksToMerge.isEmpty()) return 0
        val mergedCards: MutableList<Card> = destination.cards.toMutableList()
        for (deck in decksToMerge) {
            mergedCards.addAll(deck.cards)
        }
        val decksBackup = globalState.decks
        val cardsBackup = destination.cards
        restore = {
            destination.cards = cardsBackup
            globalState.decks = decksBackup
        }
        val deckIdsToRemove = decksToMerge.map { deck: Deck -> deck.id }
        globalState.decks = globalState.decks
            .filter { deck: Deck -> deck.id !in deckIdsToRemove }
            .toCopyableList()
        destination.cards = mergedCards.toCopyableList()
        coroutineScope.launch {
            delay(timeToKeepBackup)
            restore = null
        }
        return decksToMerge.size
    }

    private fun mergeIntoNewDeck(decksToMerge: List<Deck>, newDeckName: String): Int {
        val decksHaveTheSameExercisePreference: Boolean =
            decksToMerge.map { it.exercisePreference.id }.toSet().size == 1
        val exercisePreference: ExercisePreference =
            if (decksHaveTheSameExercisePreference) {
                decksToMerge.first().exercisePreference
            } else {
                ExercisePreference.Default
            }
        val cards: CopyableList<Card> =
            decksToMerge.flatMap { deck: Deck -> deck.cards }.toCopyableList()
        val newDeck = Deck(
            id = generateId(),
            name = newDeckName,
            cards = cards,
            exercisePreference = exercisePreference
        )
        val decksBackup = globalState.decks
        restore = {
            globalState.decks = decksBackup
        }
        val deckIdsToRemove = decksToMerge.map { deck: Deck -> deck.id }
        globalState.decks = globalState.decks
            .filter { deck: Deck -> deck.id !in deckIdsToRemove }
            .plus(newDeck)
            .toCopyableList()
        coroutineScope.launch {
            delay(timeToKeepBackup)
            restore = null
        }
        return decksToMerge.size
    }

    fun cancel() {
        backupKeeping?.cancel()
        restore?.invoke()
        backupKeeping = null
        restore = null
    }

    fun dispose() {
        coroutineScope.cancel()
    }
}

infix fun List<Deck>.into(destination: AbstractDeck) = MergeData(this, destination)