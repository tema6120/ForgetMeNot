package com.odnovolov.forgetmenot.ui.decksettings

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.room.Dao
import androidx.room.Query
import com.odnovolov.forgetmenot.db.entity.PronunciationDbEntity
import com.odnovolov.forgetmenot.entity.Pronunciation

@Dao
abstract class DeckSettingsDao {

    @Query("SELECT name FROM decks WHERE deck_id = :deckId")
    abstract fun getDeckName(deckId: Int): LiveData<String>

    @Query("SELECT random_order FROM decks WHERE deck_id = :deckId")
    abstract fun getRandomOrder(deckId: Int): LiveData<Boolean>

    fun getPronunciation(deckId: Int): LiveData<Pronunciation> {
        return Transformations.map(getPronunciationInternal(deckId)) { pronunciationDbEntity ->
            pronunciationDbEntity?.toPronunciation()
        }
    }

    @Query("SELECT * FROM pronunciations WHERE pronunciation_id = (SELECT pronunciation_id_key FROM decks WHERE deck_id = :deckId)")
    abstract fun getPronunciationInternal(deckId: Int): LiveData<PronunciationDbEntity>

    @Query("UPDATE decks SET random_order = :randomOrder WHERE deck_id = :deckId")
    abstract fun setRandomOrder(randomOrder: Boolean, deckId: Int)

    fun setPronunciation(pronunciation: Pronunciation, deckId: Int) {
        setPronunciationInternal(pronunciation.id, deckId)
    }

    @Query("UPDATE decks SET pronunciation_id_key = :pronunciationId WHERE deck_id = :deckId")
    abstract fun setPronunciationInternal(pronunciationId: Int?, deckId: Int)

}