package com.odnovolov.forgetmenot.ui.home

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.odnovolov.forgetmenot.db.AppDatabase
import com.odnovolov.forgetmenot.entity.Deck
import com.odnovolov.forgetmenot.common.getMutableLiveData

class HomeRepository(
    private val db: AppDatabase,
    private val sharedPrefs: SharedPreferences
) {
    private val dao: HomeDao = db.homeDao()
    private val backupHelper = HomeBackupHelper(db.openHelper.writableDatabase)

    fun getDecks(): LiveData<List<Deck>> {
        return dao.getDecks()
    }

    fun getDeckSorting(initialValue: DeckSorting): MutableLiveData<DeckSorting> {
        return sharedPrefs.getMutableLiveData(
            key = "deckSorting",
            initialValue = initialValue,
            toIntFunction = { deckSorting -> deckSorting.id },
            fromIntFunction = { sharedPrefsValue -> DeckSorting.getById(sharedPrefsValue) ?: initialValue }
        )
    }

    fun deleteDeckCreatingBackup(deckId: Int): Int {
        return db.runInTransaction<Int> {
            backupHelper.createDeckBackup(deckId)
            dao.deleteDeck(deckId)
        }
    }

    fun restoreLastDeletedDeck() {
        db.runInTransaction {
            backupHelper.restoreDeck()
        }
    }

}