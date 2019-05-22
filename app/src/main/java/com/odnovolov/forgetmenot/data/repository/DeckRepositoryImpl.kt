package com.odnovolov.forgetmenot.data.repository

import com.odnovolov.forgetmenot.data.db.dao.DeckDao
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.repository.Repository

class DeckRepositoryImpl(private val deckDao: DeckDao) : Repository {
    override fun insertDeck(deck: Deck): Int {
        return deckDao.insert(deck)
    }

    override fun getAllDeckNames(): List<String> {
        return deckDao.getAllDeckNames()
    }

    override fun saveDeckIdAsLastInserted(deckId: Int) {
        print("Ok!")
    }
}