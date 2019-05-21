package com.odnovolov.forgetmenot.domain.repository

import com.odnovolov.forgetmenot.domain.entity.Deck

interface Repository {
    fun insertDeck(deck: Deck): Long
    fun getAllDeckNames(): List<String>
    fun saveDeckIdAsLastInserted(deckId: Long)
}