package com.odnovolov.forgetmenot.data.repository

import com.odnovolov.forgetmenot.data.db.AppDatabase
import com.odnovolov.forgetmenot.data.db.dao.DeckDao
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.repository.DeckRepository
import io.reactivex.Observable

class DeckRepositoryImpl(private val db: AppDatabase) : DeckRepository {

    private val deckDao: DeckDao = db.deckDao()

    override fun getDeck(deckId: Int): Deck {
        return deckDao.load(deckId)
    }

    override fun saveDeck(deck: Deck): Int {
        return deckDao.insert(deck)
    }

    override fun getAllDeckNames(): List<String> {
        return deckDao.getAllDeckNames()
    }

    override fun saveLastInsertedDeckId(deckId: Int) {
        print("Ok!")
    }

    override fun observeDecks(): Observable<List<Deck>> {
        return deckDao.observeAll()
    }

    override fun createBackupAndDeleteDeckInTransaction(deckId: Int): Int {
        return db.runInTransaction<Int> {
            db.getBackuper().createDeckBackup(deckId)
            deckDao.delete(deckId)
        }
    }

    override fun restoreLastDeletedDeckFromBackup() {
        db.runInTransaction {
            db.getBackuper().restoreDeck()
        }
    }
}