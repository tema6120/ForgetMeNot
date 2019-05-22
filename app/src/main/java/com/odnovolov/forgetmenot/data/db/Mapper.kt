package com.odnovolov.forgetmenot.data.db

import com.odnovolov.forgetmenot.data.db.entity.DbCard
import com.odnovolov.forgetmenot.data.db.entity.DbDeck
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck

fun Card.toDbCard(deckId: Int): DbCard =
    DbCard(id, deckId, ordinal, question, answer)

fun Deck.toDbDeck(): DbDeck =
    DbDeck(id, name)