package com.odnovolov.forgetmenot.domain.repository

import com.odnovolov.forgetmenot.domain.entity.Deck
import io.reactivex.Observable

interface DeckRepository {
    fun insertDeck(deck: Deck): Int
    fun getAllDeckNames(): List<String>
    fun saveDeckIdAsLastInserted(deckId: Int)
    fun loadAll(): Observable<List<Deck>>
}