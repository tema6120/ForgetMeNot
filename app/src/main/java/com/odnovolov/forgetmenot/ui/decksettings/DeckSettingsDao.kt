package com.odnovolov.forgetmenot.ui.decksettings

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
abstract class DeckSettingsDao {

    @Query("SELECT name FROM decks WHERE deck_id = :deckId")
    abstract fun getDeckName(deckId: Int): LiveData<String>

    @Query("SELECT random_order FROM decks WHERE deck_id = :deckId")
    abstract fun getRandomOrder(deckId: Int): LiveData<Boolean>

    @Query("UPDATE decks SET random_order = :randomOrder WHERE deck_id = :deckId")
    abstract fun setRandomOrder(randomOrder: Boolean, deckId: Int)

}