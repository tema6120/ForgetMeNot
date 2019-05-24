package com.odnovolov.forgetmenot.data.repository

import com.odnovolov.forgetmenot.data.db.dao.DeckDao
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.repository.DeckRepository
import io.reactivex.Observable

class DeckRepositoryImpl(private val deckDao: DeckDao) : DeckRepository {

    override fun insertDeck(deck: Deck): Int {
        return deckDao.insert(deck)
    }

    override fun getAllDeckNames(): List<String> {
        return deckDao.getAllDeckNames()
    }

    override fun saveDeckIdAsLastInserted(deckId: Int) {
        print("Ok!")
    }

    override fun loadAll(): Observable<List<Deck>> {
        return deckDao.loadAll()
    }
}