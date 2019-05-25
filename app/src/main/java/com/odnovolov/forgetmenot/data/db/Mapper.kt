package com.odnovolov.forgetmenot.data.db

import com.odnovolov.forgetmenot.data.db.entity.CardDbRow
import com.odnovolov.forgetmenot.data.db.entity.DeckDbRow
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck

fun Card.toDbCard(deckId: Int): CardDbRow =
    CardDbRow(id, deckId, ordinal, question, answer)

fun Deck.toDbDeck(): DeckDbRow =
    DeckDbRow(id, name)

fun CardDbRow.toCard(): Card =
    Card(id, ordinal, question, answer)

fun DeckDbRow.toDeck(cards: List<Card>): Deck =
        Deck(id, name, cards)