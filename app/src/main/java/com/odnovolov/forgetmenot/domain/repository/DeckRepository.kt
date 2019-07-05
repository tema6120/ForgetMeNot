package com.odnovolov.forgetmenot.domain.repository

import com.odnovolov.forgetmenot.domain.entity.Deck
import io.reactivex.Observable

interface DeckRepository {
    // Create
    fun saveDeck(deck: Deck): Int
    fun saveLastInsertedDeckId(deckId: Int)

    // Read
    fun getAllDeckNames(): List<String>
    fun observeDecks(): Observable<List<Deck>>
    fun getDeck(deckId: Int): Deck

    // Delete
    fun createBackupAndDeleteDeckInTransaction(deckId: Int) : Int
    fun restoreLastDeletedDeckFromBackup()
}