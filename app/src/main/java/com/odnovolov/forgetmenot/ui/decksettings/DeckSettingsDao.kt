package com.odnovolov.forgetmenot.ui.decksettings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
abstract class DeckSettingsDao {

    @Query("SELECT name FROM decks WHERE deck_id = :deckId")
    abstract fun getDeckName(deckId: Int): LiveData<String>

    private val tempRandomOrder = MutableLiveData<Boolean>()
        .apply { value = true }

    fun getRandomOrder(deckId: Int): LiveData<Boolean> {
        return tempRandomOrder
    }

    fun updateRandomOrder(updatedRandomOrder: Boolean, deckId: Int) {
        tempRandomOrder.value = updatedRandomOrder
    }
}