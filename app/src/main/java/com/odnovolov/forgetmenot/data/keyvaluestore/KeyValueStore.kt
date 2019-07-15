package com.odnovolov.forgetmenot.data.keyvaluestore

import android.content.SharedPreferences
import androidx.core.content.edit
import com.odnovolov.forgetmenot.data.keyvaluestore.entity.DeckSortingKVEntity
import com.odnovolov.forgetmenot.domain.entity.DeckSorting

class KeyValueStore(private val sharedPreferences: SharedPreferences) {

    fun updateDeckSorting(deckSorting: DeckSorting) {
        val deckSortingKVEntity = DeckSortingKVEntity.fromDeckSorting(deckSorting)
        sharedPreferences.edit { putInt(PREFS_KEY_DECK_SORTING, deckSortingKVEntity.id) }
    }

    fun getDeckSorting(): DeckSorting? {
        val id = sharedPreferences.getInt(PREFS_KEY_DECK_SORTING, -1)
        return DeckSortingKVEntity.getById(id)?.toDeckSorting()
    }

    companion object {
        const val PREFS_KEY_DECK_SORTING = "Deck Sorting"
    }
}