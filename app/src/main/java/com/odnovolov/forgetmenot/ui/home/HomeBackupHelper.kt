package com.odnovolov.forgetmenot.ui.home

import androidx.sqlite.db.SupportSQLiteDatabase

class HomeBackupHelper(private val supportDb: SupportSQLiteDatabase) {

    fun createDeckBackup(deckId: Int) {
        supportDb.execSQL("DROP TABLE IF EXISTS card_backup")
        supportDb.execSQL("DROP TABLE IF EXISTS deck_backup")

        supportDb.execSQL(
            "CREATE TEMPORARY TABLE card_backup AS SELECT * FROM cards WHERE deck_id_fk = ?",
            arrayOf(deckId.toLong())
        )
        supportDb.execSQL(
            "CREATE TEMPORARY TABLE deck_backup AS SELECT * FROM decks WHERE deck_id = ?",
            arrayOf(deckId.toLong())
        )
    }

    fun restoreDeck() {
        supportDb.execSQL("INSERT INTO decks SELECT * FROM deck_backup")
        supportDb.execSQL("INSERT INTO cards SELECT * FROM card_backup")
    }
}